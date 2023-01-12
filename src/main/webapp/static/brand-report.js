function getBrandReportUrl() {
  var baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/brand';
}

function fetchBrandReport(onSuccess) {
  var $form = $('#brand-form');
  var json = toJson($form);
  var url = getBrandReportUrl();
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

function displayBrandReport(data) {
  const $tbody = $('#brand-report-table').find('tbody');
  $tbody.empty();

  data.forEach((item) => {
    const row = `
        <tr>
            <td>${item.id}</td>
            <td>${item.brand}</td>
            <td>${item.category} </td>
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
  fetchBrandReport(displayBrandReport);
}

//INITIALIZATION CODE
function init() {
  $('#filter-sales-report').click(showReport);
  $('#display-filter-btn').click(dispalyFilterModal);
  $('#nav-reports').addClass('active-nav');
  showReport();
}

$(document).ready(init);
