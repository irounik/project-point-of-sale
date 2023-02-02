function postCall(url, json, onSuccess, onError) {
  if (!onError) {
    onError = handleAjaxError;
  }
  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: onSuccess,
    error: onError,
  });
}

function getCall(url, onSuccess, onError) {
  if (!onError) {
    onError = handleAjaxError;
  }
  $.ajax({
    url: url,
    type: 'GET',
    success: onSuccess,
    error: onError,
  });
}

function putCall(url, json, onSuccess, onError) {
  if (!onError) {
    onError = handleAjaxError;
  }
  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: onSuccess,
    error: onError,
  });
}
