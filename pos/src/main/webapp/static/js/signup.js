function getBaseUrl() {
  return $('meta[name=baseUrl]').attr('content');
}

const URLS = {
  home: getBaseUrl() + '/ui/home',
  signupApi: getBaseUrl() + '/session/signup',
};

function signupCall() {
  $form = $('#signup-form');
  const json = toJson($form);
  const url = URLS.signupApi;
  postCall(url, json, () => (window.location = URLS.home));
}

function init() {
  $('#signup-btn').click(signupCall);
}

$(document).ready(init);
