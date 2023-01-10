function getBrandUrl() {
  var baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/brands';
}

//BUTTON ACTIONS
function addBrand() {
  //Set the values to update
  var $form = $('#add-brand-form');
  var json = toJson($form);
  var url = getBrandUrl();

  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function () {
      $('#add-brand-modal').modal('toggle');
      getBrandList();
    },
    error: handleAjaxError,
  });

  return false;
}

function updateBrand(event) {
  $('#edit-brand-modal').modal('toggle');
  //Get the ID
  var id = $('#brand-edit-form input[name=id]').val();
  var url = getBrandUrl() + '/' + id;

  //Set the values to update
  var $form = $('#brand-edit-form');
  var json = toJson($form);

  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function (response) {
      getBrandList();
    },
    error: handleAjaxError,
  });

  return false;
}

function getBrandList() {
  var url = getBrandUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayBrandList(data);
    },
    error: handleAjaxError,
  });
}

function deleteBrand(id) {
  var url = getBrandUrl() + '/' + id;

  $.ajax({
    url: url,
    type: 'DELETE',
    success: function (data) {
      getBrandList();
    },
    error: handleAjaxError,
  });
}

// FILE UPLOAD METHODS
var fileData = [];
var errorData = [];
var processCount = 0;

function processData() {
  var file = $('#brandFile')[0].files[0];
  readFileData(file, readFileDataCallback);
}

function readFileDataCallback(results) {
  fileData = results.data;
  uploadRows();
}

function uploadRows() {
  //Update progress
  updateUploadDialog();
  //If everything processed then return
  if (processCount == fileData.length) {
    return;
  }

  //Process next row
  var row = fileData[processCount];
  processCount++;

  var json = JSON.stringify(row);
  var url = getBrandUrl();

  //Make ajax call
  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function (response) {
      uploadRows();
    },
    error: function (response) {
      row.error = response.responseText;
      errorData.push(row);
      uploadRows();
    },
  });
}

function downloadErrors() {
  writeFileData(errorData);
}

//UI DISPLAY METHODS

function displayBrandList(data) {
  var $tbody = $('#brand-table').find('tbody');
  $tbody.empty();
  for (var i in data) {
    var e = data[i];
    var row = `
        <tr>
            <td>${e.id}</td>
            <td>${e.name}</td>
            <td>${e.category}</td>
            <td>
                <button class="btn btn-outline-primary" onclick="displayEditBrand(${e.id})">
                  Edit
                </button>
            </td>
        </tr>
    `;
    $tbody.append(row);
  }
}

function displayEditBrand(id) {
  var url = getBrandUrl() + '/' + id;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayBrand(data);
    },
    error: handleAjaxError,
  });
}

function resetUploadDialog() {
  //Reset file name
  var $file = $('#brandFile');
  $file.val('');
  $('#brandFileName').html('Choose File');
  //Reset various counts
  processCount = 0;
  fileData = [];
  errorData = [];
  //Update counts
  updateUploadDialog();
}

function updateUploadDialog() {
  $('#rowCount').html('' + fileData.length);
  $('#processCount').html('' + processCount);
  $('#errorCount').html('' + errorData.length);
}

function updateFileName() {
  var $file = $('#brandFile');
  var fileName = $file.val();
  $('#brandFileName').html(fileName);
}

function displayUploadData() {
  resetUploadDialog();
  $('#upload-brand-modal').modal('toggle');
}

function displayBrand(data) {
  $('#brand-edit-form input[name=name]').val(data.name);
  $('#brand-edit-form input[name=category]').val(data.category);
  $('#brand-edit-form input[name=id]').val(data.id);
  $('#edit-brand-modal').modal('toggle');
}

function displayAddBrand() {
  $('#inputName').val('');
  $('#inputCategory').val('');
  $('#add-brand-modal').modal('toggle');
}

//INITIALIZATION CODE
function init() {
  $('#add-brand').click(addBrand);
  $('#update-brand').click(updateBrand);
  $('#upload-data').click(displayUploadData);
  $('#display-add-brand').click(displayAddBrand);
  $('#process-data').click(processData);
  $('#download-errors').click(downloadErrors);
  $('#brandFile').on('change', updateFileName);
  $('#upload-brand-modal').on('hidden.bs.modal', getBrandList);
  $('#edit-brand-modal').on('hidden.bs.modal', getBrandList);
  $('#add-brand-modal').on('hidden.bs.modal', getBrandList);
  $('#nav-brands').addClass('active-nav');
}

$(document).ready(init);
$(document).ready(getBrandList);
