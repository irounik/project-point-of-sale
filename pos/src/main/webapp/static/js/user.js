function getUserUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/admin/user';
}

//BUTTON ACTIONS
function addUser() {
  //Set the values to update
  const $form = $('#user-form');
  const json = toJson($form);
  const url = getUserUrl();

  postCall(url, json, () => {
    notifySuccess('User was added sucessfully!');
    hideUserModal();
  });

  return false;
}

function hideUserModal() {
  $('#add-user-modal').modal('toggle');
  getUserList();
}

function getUserList() {
  const url = getUserUrl();
  getCall(url, displayUserList);
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

  users.forEach((user, index) => {
    const row = `<tr>
		<td>${index + 1}</td>
		<td>${user.email}</td>
    <td>${user.role}</td>
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
