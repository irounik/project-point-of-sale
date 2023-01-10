function getUserUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/admin/user';
}

//BUTTON ACTIONS
function addUser(onSuccess) {
  //Set the values to update
  const $form = $('#user-form');
  const json = toJson($form);
  const url = getUserUrl();

  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: onSuccess,
    error: handleAjaxError,
  });

  return false;
}

function getUserList() {
  const url = getUserUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayUserList(data);
    },
    error: handleAjaxError,
  });
}

function deleteUser(id) {
  const url = getUserUrl() + '/' + id;

  $.ajax({
    url: url,
    type: 'DELETE',
    success: function (data) {
      getUserList();
    },
    error: handleAjaxError,
  });
}

//UI DISPLAY METHODS

function displayUserList(users) {
  const $tbody = $('#user-table').find('tbody');
  $tbody.empty();

  users.forEach((user) => {
    const row = `<tr>
		<td>${user.id}</td>
		<td>${user.email}</td>
		<td>
			<button class="btn btn-outline-danger" onclick="deleteUser(${user.id})">Delete</button>
		</td>
	</tr>`;
    $tbody.append(row);
  });
}

function displayAddUser() {
  $('#inputEmail').val('');
  $('#inputPassword').val('');
  $('#add-user-modal').modal('toggle');
}

//INITIALIZATION CODE
function init() {
  $('#add-user-btn').click(addUser);
  $('#refresh-data').click(getUserList);
  $('#nav-admin').addClass('active-nav');
  $('#display-add-user').click(displayAddUser);
}

$(document).ready(init);
$(document).ready(getUserList);
