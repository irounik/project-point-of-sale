// API Calls
function getBaseUrl() {
  return $('meta[name=baseUrl]').attr('content');
}

function getOrderUrl() {
  return getBaseUrl() + '/api/orders';
}

function getProductUrl() {
  return getBaseUrl() + '/api/products';
}

function getOrderList() {
  const url = getOrderUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      displayOrderList(data);
    },
    error: handleAjaxError,
  });
}

function getProductByBarcode(barcode, onSuccess) {
  const url = getProductUrl() + '?barcode=' + barcode;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      onSuccess(data[0]);
    },
    error: handleAjaxError,
  });
}

function fetchOrderDetails(id, onSuccess) {
  const url = getOrderUrl() + '/' + id;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      onSuccess(data);
    },
    error: handleAjaxError,
  });
}

//UI DISPLAY METHODS
let orderItems = [];

function getCureentOrderItem() {
  return {
    barcode: $('#inputBarcode').val(),
    quantity: Number.parseInt($('#inputQuantity').val()),
    sellingPrice: $('#inputSellingPrice').val(),
  };
}

function addItem(item) {
  const index = orderItems.findIndex((it) => it.productId === Number.parseInt(item.productId));
  if (index == -1) {
    orderItems.push(item);
  } else {
    orderItems[index].quantity += item.quantity;
  }
}

function isInvalidInput(item) {
  if (!item.barcode) {
    $.notify('Please input a valid barcode!', 'error');
    return true;
  }

  if (!item.quantity || item.quantity <= 0) {
    $.notify('Quantity must be positve!', 'error');
    return true;
  }

  return false;
}

function addOrderItem() {
  const item = getCureentOrderItem();
  if (isInvalidInput(item)) return;

  getProductByBarcode(item.barcode, (product) => {
    if (item.sellingPrice > product.price) {
      $.notify(`Selling price can't be more than MRP, that is: ` + product.price, 'error');
      return;
    }

    addItem({
      productId: product.id,
      barcode: product.barcode,
      name: product.name,
      sellingPrice: item.sellingPrice,
      quantity: item.quantity,
    });

    displayCreateOrderItems(orderItems);
    resetAddItemForm();
  });
}

function onQuantityChanged(productId) {
  const index = orderItems.findIndex((item) => item.productId === Number.parseInt(productId));
  if (index == -1) return;

  const newQuantity = $(`#order-item-${productId}`).val();
  orderItems[index].quantity = Number.parseInt(newQuantity);
}

function onPriceChanged(productId) {
  const index = orderItems.findIndex((it) => it.productId == Number.parseInt(productId));
  if (index == -1) return;

  const newPrice = $(`#order-item-sellingPrice-${productId}`).val();
  orderItems[index].sellingPrice = Number.parseFloat(newPrice);
}

function displayCreateOrderItems(data) {
  const $tbody = $('#create-order-table').find('tbody');
  $tbody.empty();

  data.forEach((item, index) => {
    const row = `
      <tr>
        <td>${index + 1}</td>
        <td class="barcodeData">${item.barcode}</td>
        <td>${item.name}</td>
        <td>
          <input 
            id="order-item-sellingPrice-${item.productId}"
            type="number" 
            class="form-controll 
            quantityData" 
            value="${item.sellingPrice}"
            onchange="onPriceChanged('${item.productId}')"  
            style="width:95%" min="1">
        </td>
        <td>
          <input 
            id="order-item-${item.productId}"
            type="number" 
            class="form-controll 
            quantityData" 
            value="${item.quantity}"
            onchange="onQuantityChanged('${item.productId}')"  
            style="width:70%" min="1" max="1000000">
        </td>
        <td>
          <button onclick="deleteOrderItem('${item.productId}')" class="btn btn-outline-danger">Delete</button>
        </tb>
      </tr>
    `;

    $tbody.append(row);
  });
}

function editOrder(id) {
  fetchOrderDetails(id, (order) => {
    displayCreationModal(UPDATE_MODAL_TYPE.edit, id);
    orderItems = order.items;
    displayCreateOrderItems(orderItems);
  });
}

function deleteOrderItem(productId) {
  const index = orderItems.findIndex((it) => it.productId === Number.parseInt(productId));
  if (index == -1) return;
  orderItems.splice(index, 1);
  displayCreateOrderItems(orderItems);
}

function resetAddItemForm() {
  $('#inputBarcode').val('');
  $('#inputQuantity').val('');
  $('#inputSellingPrice').val('');
}

function resetModal() {
  resetAddItemForm();
  orderItems = [];
  displayCreateOrderItems(orderItems);
}

function getFormattedDate(timeUTC) {
  const [year, month, day, hour, min, sec] = timeUTC;
  const ist = new Date(`${month}/${day}/${year} ${hour}:${min}:${sec} UTC`);

  const doubleDigit = (digit) => {
    if (digit.toString().length < 2) {
      return `0${digit}`;
    }
    return digit;
  };

  const dformat =
    [doubleDigit(ist.getDate()), doubleDigit(ist.getMonth() + 1), ist.getFullYear()].join('/') +
    ' ' +
    [doubleDigit(ist.getHours()), doubleDigit(ist.getMinutes()), doubleDigit(ist.getSeconds())].join(':');

  return dformat;
}

function downloadInvoice(orderId) {
  const url = getOrderUrl() + '/invoice/' + orderId;
  $.ajax({
    url: url,
    type: 'GET',
    xhrFields: {
      responseType: 'blob',
    },
    headers: {
      'Content-Type': 'application/json',
    },
    success: (pdfBlob) => {
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(pdfBlob);
      link.download = 'invoice_' + orderId + '_' + new Date().getTime() + '.pdf';
      link.click();
    },
    error: handleAjaxError,
  });
}

function displayOrderList(orders) {
  const $tbody = $('#order-table').find('tbody');
  $tbody.empty();

  orders.forEach((order, index) => {
    const formattedDate = getFormattedDate(order.time);
    const row = `
        <tr>
            <td>${index + 1}</td>
            <td>${formattedDate}</td>
            <td>
                <button class="btn btn-outline-primary px-3" onclick="showDetails(${order.id})">
                  View
                </button>
                <button class="btn btn-outline-primary px-4" onclick="editOrder(${order.id})">
                  Edit
                </button>
                <button class="btn btn-outline-primary px-4" onclick="downloadInvoice(${order.id})">
                  Invoice
                </button>
            </td>
        </tr>
    `;
    $tbody.append(row);
  });
}

function showDetails(id) {
  fetchOrderDetails(id, (data) => {
    displayDetailsModal(data);
  });
}

function displayDetailsModal(orderDetails) {
  const table = $('#order-details-table');
  const $tbody = table.find('tbody');
  $tbody.empty();

  $('#order-id-text').text('Order ID: ' + orderDetails.orderId);
  $('#order-time-text').text('Time: ' + getFormattedDate(orderDetails.time));

  let totalPrice = 0;
  let totalQuantity = 0;

  orderDetails.items.forEach((item, index) => {
    totalPrice += item.sellingPrice * item.quantity;
    totalQuantity += item.quantity;

    const row = `
      <tr>
        <td>${Number.parseInt(index) + 1}</td>
        <td class="barcodeData">${item.barcode}</td>
        <td>${item.name}</td>
        <td >${item.sellingPrice}</td>
        <td>${item.quantity}</td>
        <td>${item.quantity * item.sellingPrice}</td>
      </tr>
    `;
    $tbody.append(row);
  });

  $tbody.append(
    `
    <tr class="tfoot">
      <td>Total</td>
      <td></td>
      <td></td>
      <td></td>
      <td>${totalQuantity}</td>
      <td>Rs ${totalPrice}/-</td>
    </tr>
    `
  );

  $('#order-details-modal').modal('toggle');
}

function createNewOrder() {
  resetModal();
  displayCreationModal(UPDATE_MODAL_TYPE.create);
}

const UPDATE_MODAL_TYPE = {
  create: 'create',
  edit: 'edit',
};

function editOrderCall(id) {
  const url = getOrderUrl() + '/' + id;
  const json = JSON.stringify(orderItems);

  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: () => {
      $.notify(`Order updated successfully!`, 'success');
      hideCreationModal();
    },
    error: handleAjaxError,
  });
}

function displayCreationModal(type, orderId) {
  if (type === UPDATE_MODAL_TYPE.edit) {
    $('#post-modal-title').text('Edit Order: ' + orderId);
    $('#place-order-btn')
      .unbind()
      .text('Update Order')
      .click(() => editOrderCall(orderId));
  } else {
    $('#post-modal-title').text('Create Order');
    $('#place-order-btn').unbind().text('Place Order').click(placeNewOrder);
  }

  $('#create-order-modal').modal({ backdrop: 'static', keyboard: false }, 'show');
}

function hideCreationModal() {
  $('#create-order-modal').modal('toggle');
  getOrderList();
}

//INITIALIZATION CODE
function init() {
  $('#add-order-item').click(addOrderItem);
  $('#create-order').click(createNewOrder);
  $('#refresh-data').click(getOrderList);
  $('#place-order-btn').click(placeNewOrder);
  $('#nav-orders').addClass('active-nav');
}

$(document).ready(init);
$(document).ready(getOrderList);

// Place Order
function placeNewOrder() {
  const data = orderItems.map((it) => {
    return {
      barcode: it.barcode,
      quantity: it.quantity,
      sellingPrice: it.sellingPrice,
    };
  });

  const json = JSON.stringify(data);
  placeOrder(json, hideCreationModal);
}

//BUTTON ACTIONS
function placeOrder(json, onSuccess) {
  //Set the values to update
  const url = getOrderUrl();

  $.ajax({
    url: url,
    type: 'POST',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: () => {
      $.notify('Order placed successfully!', 'success');
      onSuccess();
    },
    error: handleAjaxError,
  });

  return false;
}
