const reportsBaseUrl = $('meta[name=baseUrl]').attr('content') + '/ui/reports';

const REPORT_URLS = {
  brandReportUrl: reportsBaseUrl + '/brand',
  salesReportUrl: reportsBaseUrl + '/sales',
  perDaySaleReportUrl: reportsBaseUrl + '/per-day-sale',
  inventoryReportUrl: reportsBaseUrl + '/inventory',
};

function redirctTo(url) {
  location.href = url;
}

function init() {
  $('#brand-report-card').click(() => {
    redirctTo(REPORT_URLS.brandReportUrl);
  });

  $('#sales-report-card').click(() => {
    redirctTo(REPORT_URLS.salesReportUrl);
  });

  $('#per-day-sale-report-card').click(() => {
    redirctTo(REPORT_URLS.perDaySaleReportUrl);
  });

  $('#inventory-report-card').click(() => {
    redirctTo(REPORT_URLS.inventoryReportUrl);
  });

  $('#nav-reports').addClass('active-nav');
}

$(document).ready(init);
