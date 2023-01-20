function getInventoryReportUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/inventory';
}

function fetchInventoryReport(onSuccess) {
  const url = getInventoryReportUrl();

  $.ajax({
    url: url,
    type: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    success: onSuccess,
    error: handleAjaxError,
  });
}

function displayInventoryReport(data) {
  const $tbody = $('#inventory-report-table').find('tbody');
  $tbody.empty();

  data.forEach((item, index) => {
    const row = `
        <tr>
            <td>${index + 1}</td>
            <td>${item.brand}</td>
            <td>${item.category}</td>
            <td>${item.quantity}
        </tr>
      `;
    $tbody.append(row);
  });
}

function showReport() {
  fetchInventoryReport(displayInventoryReport);
}

//INITIALIZATION CODE
function init() {
  $('#nav-reports').addClass('active-nav');
  showReport();
}

$(document).ready(init);
