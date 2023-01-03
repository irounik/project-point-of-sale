// API Calls
function getBaseUrl() {
  return $('meta[name=baseUrl]').attr('content');
}

function getOrderUrl() {
  return getBaseUrl() + '/api/orders/';
}

function getProductUrl() {
  return getBaseUrl() + '/api/products';
}

function getOrderList() {
  var url = getOrderUrl();
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      console.log(data);
      displayOrderList(data);
    },
    error: handleAjaxError,
  });
}

function getProductByBarcode(barcode, onSuccess) {
  const url = getProductUrl() + '/' + barcode;
  $.ajax({
    url: url,
    type: 'GET',
    success: function (data) {
      onSuccess(data);
    },
    error: handleAjaxError,
  });
}

function fetchOrderDetails(id, onSuccess) {
  var url = getOrderUrl() + id;
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
  };
}

function addItem(item) {
  const index = orderItems.findIndex((it) => it.barcode === item.barcode);
  if (index == -1) {
    orderItems.push(item);
  } else {
    orderItems[index].quantity += item.quantity;
  }
}

function isInvalidInput(item) {
  if (!item.barcode) {
    alert('Please input a valid barcode!');
    return true;
  }

  if (item.quantity <= 0) {
    alert('Quantity must be positve!');
    return true;
  }

  return false;
}

function addOrderItem() {
  const item = getCureentOrderItem();
  if (isInvalidInput(item)) return;

  getProductByBarcode(item.barcode, (product) => {
    addItem({
      barcode: product.barcode,
      name: product.name,
      price: product.price,
      quantity: item.quantity,
    });

    displayCreateOrderItems(orderItems);
    resetAddItemForm();
  });
}

function onQuantityChanged(barcode) {
  const index = orderItems.findIndex((it) => it.barcode === barcode);
  if (index == -1) return;

  const newQuantity = $(`#order-item-${barcode}`).val();
  orderItems[index].quantity = Number.parseInt(newQuantity);
}

function displayCreateOrderItems(data) {
  const $tbody = $('#create-order-table').find('tbody');
  $tbody.empty();

  for (let i in data) {
    const item = data[i];
    const row = `
      <tr>
        <td>${Number.parseInt(i) + 1}</td>
        <td class="barcodeData">${item.barcode}</td>
        <td>${item.name}</td>
        <td >${item.price}</td>
        <td>
          <input 
            id="order-item-${item.barcode}"
            type="number" 
            class="form-controll 
            quantityData" 
            value="${item.quantity}"
            onchange="onQuantityChanged('${item.barcode}')"  
            style="width:70%" min="1">
        </td>
        <td>
          <button onclick="deleteOrderItem('${item.barcode}')" class="btn btn-outline-danger">Delete</button>
        </tb>
      </tr>
    `;

    $tbody.append(row);
  }
}

function editOrder(id) {
  fetchOrderDetails(id, (order) => {
    displayCreationModal(UPDATE_MODAL_TYPE.edit, id);
    orderItems = order.items;
    displayCreateOrderItems(orderItems);
  });
}

function deleteOrderItem(barcode) {
  const index = orderItems.findIndex((it) => it.barcode === barcode);
  if (index == -1) return;
  orderItems.splice(index, 1);
  displayCreateOrderItems(orderItems);
}

function resetAddItemForm() {
  $('#inputBarcode').val('');
  $('#inputQuantity').val('');
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

function displayOrderList(orders) {
  var $tbody = $('#order-table').find('tbody');
  $tbody.empty();

  orders.forEach((order) => {
    const formattedDate = getFormattedDate(order.time);
    var row = `
        <tr>
            <td>${order.id}</td>
            <td>${formattedDate}</td>
            <td>
                <button class="btn btn-outline-primary px-3" onclick="editOrder(${order.id})">
                  Edit
                </button>
            </td>
        </tr>
    `;
    $tbody.append(row);
  });
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
  const url = getOrderUrl() + id;
  const json = JSON.stringify(orderItems);

  $.ajax({
    url: url,
    type: 'PUT',
    data: json,
    headers: {
      'Content-Type': 'application/json',
    },
    success: () => {
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
    success: onSuccess,
    error: handleAjaxError,
  });

  return false;
}
