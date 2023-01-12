function getSalesReportUrl() {
  var baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/per-day-sale';
}

function fetchSalesReport(onSuccess) {
  var $form = $('#sales-form');
  var json = toJson($form);
  var url = getSalesReportUrl();
  console.log(url);

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
}

function formatDate(date) {
  const [year, month, day] = date;
  return `${day}:${month}:${year}`;
}

function displaySalesReport(data) {
  const $tbody = $('#sales-table').find('tbody');
  $tbody.empty();

  data.forEach((item) => {
    const row = `
      <tr>
          <td>${formatDate(item.date)}</td>
          <td>${item.ordersCount}</td>
          <td>${item.itemsCount}</td>
          <td>${item.totalRevenue} </td>
      </tr>
    `;
    $tbody.append(row);
  });
}

function resetFilterModal() {
  $('sales-form').trigger('reset');
}

function dispalyFilterModal() {
  resetFilterModal();
  $('#filter-modal').modal('toggle');
}

function showReport() {
  fetchSalesReport(displaySalesReport);
}

//INITIALIZATION CODE
function init() {
  $('#filter-sales-report').click(showReport);
  $('#display-filter-btn').click(dispalyFilterModal);
  $('#nav-reports').addClass('active-nav');
  showReport();
}

$(document).ready(init);
