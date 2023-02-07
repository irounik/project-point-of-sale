function getBaseUrl() {
  return $('meta[name=baseUrl]').attr('content');
}

const URLS = {
  home: getBaseUrl() + '/ui/home',
  signupApi: getBaseUrl() + '/session/signup',
};

function isValidForm(signupForm) {
  const { email, password } = signupForm;
  if (!email) {
    notifyError("Email can't be blank!");
    return false;
  }

  if (!email.match(/^[a-z0-9.]+@[a-z]+\.[a-z]{2,3}$/)) {
    notifyError('Please enter a valid email!');
    return false;
  }

  if (!password) {
    notifyError("Password can't be blank!");
    return false;
  }

  if (password.length < 8) {
    notifyError('Password must have 8 or more characters!');
    return false;
  }

  if (!password.match(/(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\d)(?=.*?[#?!@$%^&*-]).{8,}/)) {
    notifyError('Password must have at least one capital, small & special character!');
    return false;
  }

  return true;
}

function signupCall() {
  $form = $('#signup-form');
  let jsonString = toJson($form);
  const formJson = JSON.parse(jsonString);
  if (!isValidForm(formJson)) return;

  const url = URLS.signupApi;
  postCall(url, jsonString, () => (window.location = URLS.home));
}

function init() {
  $('#signup-btn').click(signupCall);
}

$(document).ready(init);
