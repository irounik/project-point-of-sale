function getSalesReportUrl() {
  var baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/sales';
}

function filterSalesReport(onSuccess) {
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

function displaySalesReport(data) {
  const $tbody = $('#sales-table').find('tbody');
  $tbody.empty();

  data.forEach((item, index) => {
    const row = `
      <tr>
          <td>${index + 1}</td>
          <td>${item.brandName}</td>
          <td>${item.category}</td>
          <td>${item.quantity}</td>
          <td>${item.revenue} </td>
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

//INITIALIZATION CODE
function init() {
  filterSalesReport((data) => {
    const brands = data.map((it) => {
      return { brand: it.brandName, category: it.category };
    });
    setupBrandCategoryDropdown(brands, '#brand-name-selection', '#brand-category-selection');
    displaySalesReport(data);
  });

  $('#filter-sales-report').click(() => filterSalesReport(displaySalesReport));
  $('#display-filter-btn').click(dispalyFilterModal);
  $('#nav-reports').addClass('active-nav');
}

$(document).ready(init);
