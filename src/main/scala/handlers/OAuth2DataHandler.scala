package handlers

import models.{ Account, OAuthAccessToken }
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaoauth2.provider.{ ClientCredentialsRequest, InvalidClient, PasswordRequest, _ }

class OAuth2DataHandler(
    val oAuthClientsService: OAuthClientsService,
    val oAuthAccessTokensService: OAuthAccessTokensService,
    val accountsService: AccountsService
) extends DataHandler[Account] {

  override def validateClient(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = {
    maybeCredential.fold(Future.successful(false))(
      clientCredential => oAuthClientsService.validate(
        clientCredential.clientId,
        clientCredential.clientSecret.getOrElse(""), request.grantType
      )
    )
  }

  override def getStoredAccessToken(authInfo: AuthInfo[Account]): Future[Option[AccessToken]] = {
    oAuthAccessTokensService.findByAuthorized(authInfo.user, authInfo.clientId.getOrElse("")).map(_.map(toAccessToken))
  }

  private val accessTokenExpireSeconds = 3600

  private def toAccessToken(accessToken: OAuthAccessToken) = {
    AccessToken(
      accessToken.accessToken,
      Some(accessToken.refreshToken),
      None,
      Some(accessTokenExpireSeconds),
      accessToken.createdAt
    )
  }

  override def createAccessToken(authInfo: AuthInfo[Account]): Future[AccessToken] = {
    authInfo.clientId.fold(Future.failed[AccessToken](new InvalidRequest())) { clientId =>
      (for {
        clientOpt <- oAuthClientsService.findByClientId(clientId)
        toAccessToken <- oAuthAccessTokensService.create(authInfo.user, clientOpt.get).map(toAccessToken) if clientOpt.isDefined
      } yield toAccessToken).recover { case _ => throw new InvalidRequest() }
    }
  }

  override def findUser(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[Account]] = {
    request match {
      case request: PasswordRequest =>
        accountsService.authenticate(request.username, request.password)
      case request: ClientCredentialsRequest =>
        maybeCredential.fold(Future.failed[Option[Account]](new InvalidRequest())) { clientCredential =>
          for {
            maybeAccount <- oAuthClientsService.findClientCredentials(
              clientCredential.clientId,
              clientCredential.clientSecret.getOrElse("")
            )
          } yield maybeAccount
        }
      case _ =>
        Future.successful(None)

    }
  }

  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[Account]]] = {
    oAuthAccessTokensService.findByRefreshToken(refreshToken).flatMap {
      case Some(accessToken) =>
        for {
          account <- accountsService.findAccountById(accessToken.accountId)
          client <- oAuthClientsService.findClientByClientId(accessToken.oauthClientId)
        } yield {
          Some(AuthInfo(
            user = account.get,
            clientId = Some(client.get.clientId),
            scope = None,
            redirectUri = client.get.redirectUri
          ))
        }
      case None => Future.failed(new InvalidRequest())
    }
  }

  override def refreshAccessToken(authInfo: AuthInfo[Account], refreshToken: String): Future[AccessToken] = {
    authInfo.clientId.fold(Future.failed[AccessToken](new InvalidRequest())) { clientId =>
      (for {
        clientOpt <- oAuthClientsService.findByClientId(clientId)
        toAccessToken <- oAuthAccessTokensService.refresh(authInfo.user, clientOpt.get).map(toAccessToken) if clientOpt.isDefined
      } yield toAccessToken).recover { case _ => throw new InvalidClient() }
    }
  }

  //START: I decided to skip code flow implementation
  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[Account]]] = ???

  override def deleteAuthCode(code: String): Future[Unit] = ???

  //END

  override def findAccessToken(token: String): Future[Option[AccessToken]] = {
    oAuthAccessTokensService.findByAccessToken(token).map(_.map(toAccessToken))
  }

  override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[Account]]] = {
    oAuthAccessTokensService.findByAccessToken(accessToken.token).flatMap {
      case Some(accessToken) =>
        for {
          account <- accountsService.findAccountById(accessToken.accountId)
          client <- oAuthClientsService.findClientByClientId(accessToken.oauthClientId)
        } yield {
          Some(AuthInfo(
            user = account.get,
            clientId = Some(client.get.clientId),
            scope = None,
            redirectUri = client.get.redirectUri
          ))
        }
      case None => Future.failed(new InvalidRequest())
    }
  }
}
