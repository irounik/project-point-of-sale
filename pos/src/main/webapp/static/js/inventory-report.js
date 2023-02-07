function getInventoryReportUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/inventory';
}

function fetchInventoryReport(onSuccess) {
  const url = getInventoryReportUrl() + '/';
  const $form = $('#inventory-filter-form');
  const json = toJson($form);
  postCall(url, json, onSuccess);
}

function fetchBrandsCall(json, onSuccess) {
  const url = getBrandReportUrl() + '/';
  console.log(url);
  postCall(url, json, onSuccess);
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

function filterInventoryReport() {
  fetchInventoryReport((data) => {
    notifySuccess('Filter applied sucessfylly!');
    $('#filter-modal').modal('toggle');
    displayInventoryReport(data);
  });
}

function initialSetup() {
  fetchInventoryReport((data) => {
    const brands = data.map((it) => {
      return { brand: it.brand, category: it.category };
    });
    setupBrandCategoryDropdown(brands, '#brand-name-selection', '#brand-category-selection');
    displayInventoryReport(data);
  });
}

function toggleFilterModal() {
  $('#filter-modal').modal('toggle');
}

//INITIALIZATION CODE
function init() {
  $('#nav-reports').addClass('active-nav');
  $('#filter-inventory-report').click(filterInventoryReport);
  $('#display-filter-btn').click(toggleFilterModal);
  initialSetup();
}

$(document).ready(init);
