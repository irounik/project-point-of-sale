const baseUrl = $('meta[name=baseUrl]').attr('content');

const UI_URLS = {
  brandUrl: baseUrl + '/ui/brands',
  productUrl: baseUrl + '/ui/products',
  inventoryUrl: baseUrl + '/ui/inventory',
  ordersUrl: baseUrl + '/ui/orders',
  reportUrl: baseUrl + '/ui/reports',
  adminUrl: baseUrl + '/ui/admin',
};

function redirectTo(url) {
  location.href = url;
}

function init() {
  $('#brands-card').click(() => redirectTo(UI_URLS.brandUrl));
  $('#products-card').click(() => redirectTo(UI_URLS.productUrl));
  $('#inventory-card').click(() => redirectTo(UI_URLS.inventoryUrl));
  $('#orders-card').click(() => redirectTo(UI_URLS.ordersUrl));
  $('#reports-card').click(() => redirectTo(UI_URLS.reportUrl));
  $('#admin-card').click(() => redirectTo(UI_URLS.adminUrl));
  $('#ui-nav').empty();
}

$(document).ready(init);
