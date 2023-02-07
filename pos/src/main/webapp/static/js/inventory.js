function getInventoryUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/inventory';
}

function updateInventory(event) {
  //Get the ID
  const url = getInventoryUrl() + '/';

  //Set the values to update
  const $form = $('#inventory-edit-form');
  const json = toJson($form);

  putCall(url, json, () => {
    notifySuccess('Inventory updated successfully!');
    $('#edit-inventory-modal').modal('toggle');
    getInventoryList();
  });

  return false;
}

function getInventoryList() {
  const url = getInventoryUrl() + '/';
  getCall(url, displayInventoryList);
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
  const url = getInventoryUrl() + '/';

  //Make ajax call
  putCall(url, json, uploadRows, (response) => {
    row.error = response.responseText.message;
    errorData.push(row);
    uploadRows();
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
            <td ${isSupervisor() ? '' : 'hidden'}>
                <button class="btn btn-outline-primary" onclick="displayEditInventory('${item.id}')">
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
  getCall(url, displayInventory);
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
  $('#inventory-edit-form input[name=quantity]').val(data.quantity);
  $('#inventory-edit-form input[name=barcode]').val(data.barcode);
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
