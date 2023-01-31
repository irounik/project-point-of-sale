function getProductUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/products';
}

function getBrandUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/brands';
}

function getBrandList(onSuccess) {
  const url = getBrandUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: onSuccess,
    error: handleAjaxError,
  });
}

//BUTTON ACTIONS
function addProduct(event) {
  //Set the values to update
  const $form = $('#product-form');
  const json = toJson($form);
  const url = getProductUrl();

  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function () {
      getProductList();
      $('#add-product-modal').modal('toggle');
      $.notify('Product added successfully!', 'success');
    },
    error: handleAjaxError,
  });

  return false;
}

function updateProduct() {
  //Get the Barcode
  const id = $('#product-edit-form input[name=id]').val();
  const url = getProductUrl() + '/' + id;

  //Set the values to update
  const $form = $('#product-edit-form');
  const json = toJson($form);

  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: function () {
      $.notify('Product updated successfully!', 'success');
      $('#edit-product-modal').modal('toggle');
      getProductList();
    },
    error: handleAjaxError,
  });

  return false;
}

function getProductList() {
  const url = getProductUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayProductList(data);
    },
    error: handleAjaxError,
  });
}

function deleteProduct(id) {
  const url = getProductUrl() + '/' + id;

  $.ajax({
    url: url,
    type: 'DELETE',
    success: function (data) {
      getProductList();
    },
    error: handleAjaxError,
  });
}

// FILE UPLOAD METHODS
let fileData = [];
let errorData = [];
let processCount = 0;

function processData() {
  const file = $('#productFile')[0].files[0];
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
  processCount++;

  const json = JSON.stringify(row);
  const url = getProductUrl();

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
function displayProductList(products) {
  const $tbody = $('#product-table').find('tbody');
  $tbody.empty();

  products.forEach((product, index) => {
    const row = `
          <tr>
              <td>${index + 1}</td>
              <td>${product.barcode}</td>
              <td>${product.name}</td>
              <td>${product.brandName}</td>
              <td>${product.category}</td>
              <td>${product.price}</td>
              <td>
                  <button 
                    class="btn btn-outline-primary" 
                    onclick="displayEditProduct('${product.id}')">
                    Edit
                  </button>
              </td>
          </tr>
      `;
    $tbody.append(row);
  });
}

function displayEditProduct(id) {
  const url = getProductUrl() + '/' + id;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayProduct(data);
    },
    error: handleAjaxError,
  });
}

function resetUploadDialog() {
  //Reset file name
  const $file = $('#productFile');
  $file.val('');
  $('#productFileName').html('Choose File');
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
  const $file = $('#productFile');
  const fileName = $file.val();
  $('#productFileName').html(fileName);
}

function displayUploadData() {
  resetUploadDialog();
  $('#upload-product-modal').modal('toggle');
}

function displayProduct(data) {
  $('#product-edit-form input[name=id]').val(data.id);
  $('#product-edit-form input[name=barcode]').val(data.barcode);
  $('#product-edit-form input[name=price]').val(data.price);
  $('#product-edit-form input[name=name]').val(data.name);

  $('#edit-brand-name-selection').val(data.brandName).change();
  $('#edit-brand-category-selection').val(data.category).change();

  $('#edit-product-modal').modal('toggle');
}

function displayAddProduct() {
  $('#product-form input[name=id]').val('');
  $('#product-form input[name=barcode]').val('');
  $('#product-form input[name=price]').val('');
  $('#product-form input[name=brand]').val('');
  $('#product-form input[name=name]').val('');
  $('#product-form input[name=category]').val('');
  $('#add-product-modal').modal('toggle');
}

//INITIALIZATION CODE
function init() {
  setupDropdown();
  $('#add-product').click(addProduct);
  $('#update-product').click(updateProduct);
  $('#refresh-data').click(getProductList);
  $('#upload-data').click(displayUploadData);
  $('#process-data').click(processData);
  $('#download-errors').click(downloadErrors);
  $('#productFile').on('change', updateFileName);
  $('#display-add-product').click(displayAddProduct);
  $('#upload-product-modal').on('hidden.bs.modal', getProductList);
  $('#edit-product-modal').on('hidden.bs.modal', getProductList);
  $('#nav-products').addClass('active-nav');
}

function setupDropdown() {
  getBrandList((brands) => {
    const brandCategory = brands.map((brandItem) => {
      return { brand: brandItem.name, category: brandItem.category };
    });
    setupBrandCategoryDropdown(brandCategory, '#brand-name-selection', '#brand-category-selection');
    setupBrandCategoryDropdown(brandCategory, '#edit-brand-name-selection', '#edit-brand-category-selection');
  });
}

$(document).ready(init);
$(document).ready(getProductList);
