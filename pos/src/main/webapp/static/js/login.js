function getBaseUrl() {
  return $('meta[name=baseUrl]').attr('content');
}

const URLS = {
  home: getBaseUrl() + '/ui/home',
  loginApi: getBaseUrl() + '/session/login',
};

function loginCall() {
  $form = $('#login-form');
  const json = toJson($form);
  const url = URLS.loginApi;
  postCall(url, json, () => (window.location = URLS.home));
}

function init() {
  $('#login-btn').click(loginCall);
}

$(document).ready(init);
