function getBrandReportUrl() {
  var baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/brand';
}

function fetchBrandReport(onSuccess) {
  const $form = $('#brand-form');
  const json = toJson($form);
  fetchBrandsCall(json, onSuccess);
}

function fetchBrandsCall(json, onSuccess) {
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

function toggleFilterModal() {
  resetFilterModal();
  $('#filter-modal').modal('toggle');
}

function showReport() {
  fetchBrandReport((brands) => {
    displayBrandReport(brands);
    toggleFilterModal();
  });
}

function initialSetup() {
  fetchBrandReport((brands) => {
    setupBrandCategoryDropdown(brands, '#brand-name-selection', '#brand-category-selection');
    displayBrandReport(brands);
  });
}

//INITIALIZATION CODE
function init() {
  $('#filter-brand-report').click(showReport);
  $('#display-filter-btn').click(toggleFilterModal);
  $('#nav-reports').addClass('active-nav');
  initialSetup();
}

$(document).ready(init);
