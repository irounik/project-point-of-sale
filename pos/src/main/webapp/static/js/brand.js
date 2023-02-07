function getBrandUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/brands';
}

//BUTTON ACTIONS
function addBrand() {
  //Set the values to update
  const $form = $('#add-brand-form');
  const json = toJson($form);
  const url = getBrandUrl() + '/';

  postCall(url, json, () => {
    $('#add-brand-modal').modal('toggle');
    notifySuccess('Brand was added successfully!');
    getBrandList();
  });

  return false;
}

function updateBrand(event) {
  //Get the ID
  const id = $('#brand-edit-form input[name=id]').val();
  const url = getBrandUrl() + '/' + id;

  //Set the values to update
  const $form = $('#brand-edit-form');
  const json = toJson($form);

  putCall(url, json, () => {
    notifySuccess('Brand updated successfully!');
    $('#edit-brand-modal').modal('toggle');
    getBrandList();
  });

  return false;
}

function getBrandList() {
  const url = getBrandUrl() + '/';
  getCall(url, displayBrandList);
}

// FILE UPLOAD METHODS
let fileData = [];
let errorData = [];
let processCount = 0;

function processData() {
  const file = $('#brandFile')[0].files[0];
  if (file.type != 'text/tab-separated-values') {
    notifyError('Wrong file type, please select a TSV file!');
    return;
  }
  readFileData(file, readFileDataCallback);
}

function readFileDataCallback(results) {
  const MAX_ROWS = 5000;

  if (results.data.length > MAX_ROWS) {
    notifyError(`File is too big! There should be less than ${MAX_ROWS} in TSV`);
    return;
  }

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
  processCount++;

  const json = JSON.stringify(row);
  const url = getBrandUrl() + '/';

  //Make ajax call
  postCall(url, json, uploadRows, (response) => {
    const error = response.responseText;

    try {
      const errJson = JSON.parse(error);
      row.error = errJson.message;
    } catch (ex) {
      row.error = 'Unknown error!';
    }

    errorData.push(row);
    uploadRows();
  });
}

function downloadErrors() {
  writeFileData(errorData);
}

//UI DISPLAY METHODS

function displayBrandList(data) {
  const $tbody = $('#brand-table').find('tbody');
  $tbody.empty();
  data.forEach((brand, index) => {
    const row = `
        <tr>
            <td>${index + 1}</td>
            <td>${brand.name}</td>
            <td>${brand.category}</td>
            <td ${isSupervisor() ? '' : 'hidden'}>
                <button class="btn btn-outline-primary" onclick="displayEditBrand(${brand.id})">
                  Edit
                </button>
            </td>
        </tr>
    `;
    $tbody.append(row);
  });
}

function displayEditBrand(id) {
  const url = getBrandUrl() + '/' + id;
  getCall(url, displayBrand);
}

function resetUploadDialog() {
  //Reset file name
  const $file = $('#brandFile');
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
  const $file = $('#brandFile');
  const fileName = $file.val();
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
