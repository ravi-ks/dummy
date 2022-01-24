import { AuthConfig } from 'angular-oauth2-oidc';

export const authConfig: AuthConfig = {
  issuer: 'https://login.microsoftonline.com/10eb05f5-7f7f-4546-ba6c-c4794a3a61ce/v2.0',
  redirectUri: window.location.origin + '/',
  clientId: 'c7a251f2-28fd-451d-b779-0fc134262973',
  responseType: 'code',
  strictDiscoveryDocumentValidation: false,
  scope: 'openid api://c7a251f2-28fd-451d-b779-0fc134262973/app',
  logoutUrl: window.location.origin + '/'
}
