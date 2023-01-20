function getSalesReportUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/per-day-sale';
}

function fetchSalesReport(onSuccess) {
  const $form = $('#sales-form');
  const jsonString = toJson($form);

  const json = JSON.parse(jsonString);

  setupDate(json);

  jsonString = JSON.stringify(json);

  const url = getSalesReportUrl();
  console.log(url);

  $.ajax({
    url: url,
    type: 'POST',
    data: jsonString,
    headers: {
      'Content-Type': 'application/json',
    },
    success: onSuccess,
    error: handleAjaxError,
  });
}

function formatDate(date) {
  const [year, month, day] = date;
  return `${day}/${month}/${year}`;
}

function getIsoDate(dateString) {
  const date = new Date(dateString);
  return date.toISOString();
}

function setupDate(json) {
  if (json.startDate) {
    json.startDate = getIsoDate(json.startDate);
  }

  if (json.endDate) {
    json.endDate = getIsoDate(json.endDate);
  }
}

function displaySalesReport(data) {
  const $tbody = $('#sales-table').find('tbody');
  $tbody.empty();

  data.forEach((item, index) => {
    const row = `
      <tr>
          <td>${index + 1}</td>
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
