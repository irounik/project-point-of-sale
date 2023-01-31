function getSalesReportUrl() {
  const baseUrl = $('meta[name=baseUrl]').attr('content');
  return baseUrl + '/api/reports/sales';
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

function filterSalesReport(onSuccess) {
  const $form = $('#sales-form');
  let jsonString = toJson($form);

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

function dispalyFilterModal() {
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

  $('#filter-sales-report').click(() =>
    filterSalesReport((data) => {
      $.notify('Filter applied sucessfylly!', 'success');
      $('#filter-modal').modal('toggle');
      displaySalesReport(data);
    })
  );
  $('#display-filter-btn').click(dispalyFilterModal);
  $('#nav-reports').addClass('active-nav');
}

$(document).ready(init);
