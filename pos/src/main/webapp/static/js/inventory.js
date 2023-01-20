function getInventoryUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/inventory';
}

function updateInventory(event) {
  $('#edit-inventory-modal').modal('toggle');
  //Get the ID
  const id = $('#inventory-edit-form input[name=barcode]').val();
  const url = getInventoryUrl() + '/' + id;

  //Set the values to update
  const $form = $('#inventory-edit-form');
  const json = toJson($form);

  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function (response) {
      $.notify('Inventory updated successfully!', 'success');
      getInventoryList();
    },
    error: handleAjaxError,
  });

  return false;
}

function getInventoryList() {
  const url = getInventoryUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayInventoryList(data);
    },
    error: handleAjaxError,
  });
}

// FILE UPLOAD METHODS
let fileData = [];
let errorData = [];
let processCount = 0;

function processData() {
  const file = $('#inventoryFile')[0].files[0];
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
  const row = fileData[processCount];
  row.quantity = Number.parseInt(row.quantity);

  processCount++;

  const json = JSON.stringify(row);
  const url = getInventoryUrl() + '/' + row.barcode;

  //Make ajax call
  $.ajax({
    url: url,
    type: 'PUT',
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

function displayInventoryList(data) {
  const $tbody = $('#inventory-table').find('tbody');
  $tbody.empty();
  data.forEach((item, index) => {
    const row = `
        <tr>
            <td>${index + 1}</td>
            <td>${item.barcode}</td>
            <td>${item.productName}</td>
            <td>${item.quantity}</td>
            <td>
                <button class="btn btn-outline-primary" onclick="displayEditInventory('${item.barcode}')">
                  Edit
                </button>
            </td>
        </tr>
      `;
    $tbody.append(row);
  });
}

function displayEditInventory(id) {
  const url = getInventoryUrl() + '/' + id;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayInventory(data);
    },
    error: handleAjaxError,
  });
}

function resetUploadDialog() {
  //Reset file name
  const $file = $('#inventoryFile');
  $file.val('');
  $('#inventoryFileName').html('Choose File');
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
  const $file = $('#inventoryFile');
  const fileName = $file.val();
  $('#inventoryFileName').html(fileName);
}

function displayUploadData() {
  resetUploadDialog();
  $('#upload-inventory-modal').modal('toggle');
}

function displayInventory(data) {
  $('#inventory-edit-form input[name=barcode]').val(data.barcode);
  $('#inventory-edit-form input[name=quantity]').val(data.quantity);
  $('#edit-inventory-modal').modal('toggle');
}

//INITIALIZATION CODE
function init() {
  $('#update-inventory').click(updateInventory);
  $('#upload-data').click(displayUploadData);
  $('#process-data').click(processData);
  $('#download-errors').click(downloadErrors);
  $('#inventoryFile').on('change', updateFileName);
  $('#edit-inventory-modal').on('hidden.bs.modal', getInventoryList);
  $('#upload-inventory-modal').on('hidden.bs.modal', getInventoryList);
  $('#nav-inventory').addClass('active-nav');
}

$(document).ready(init);
$(document).ready(getInventoryList);
