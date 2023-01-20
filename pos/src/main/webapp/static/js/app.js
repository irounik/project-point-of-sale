function toJson($form) {
  const formInputs = $form.serializeArray();
  const data = {};

  formInputs.forEach((input) => (data[input['name']] = input['value']));

  const json = JSON.stringify(data);
  return json;
}

function handleAjaxError(response) {
  try {
    const responseText = JSON.parse(response.responseText);
    $.notify(responseText.message, 'error');
  } catch (ex) {
    console.log(ex);
    $.notify('Unknown error occured!', 'error');
  }
}

function readFileData(file, callback) {
  const config = {
    header: true,
    delimiter: '\t',
    skipEmptyLines: 'greedy',
    complete: function (results) {
      callback(results);
    },
  };
  Papa.parse(file, config);
}

function writeFileData(arr) {
  const config = {
    quoteChar: '',
    escapeChar: '',
    delimiter: '\t',
  };

  let data = Papa.unparse(arr, config);
  let blob = new Blob([data], { type: 'text/tsv;charset=utf-8;' });
  let fileUrl = null;

  if (navigator.msSaveBlob) {
    fileUrl = navigator.msSaveBlob(blob, 'download.tsv');
  } else {
    fileUrl = window.URL.createObjectURL(blob);
  }
  const tempLink = document.createElement('a');
  tempLink.href = fileUrl;
  tempLink.setAttribute('download', 'download.tsv');
  tempLink.click();
}

function setupBrandCategoryDropdown(brands, brandSelectionId, categorySelectionId) {
  const brandNameSet = new Set();
  const brandCategorySet = new Set();

  brands.forEach((item) => {
    brandNameSet.add(item.brand);
    brandCategorySet.add(item.category);
  });

  const sortAndAppend = (id, set) => {
    const list = Array.from(set);
    list.sort();
    appendOptions(id, list);
  };

  sortAndAppend(brandSelectionId, brandNameSet);
  sortAndAppend(categorySelectionId, brandCategorySet);
}

function appendOptions(selectElementId, options) {
  const $selectElement = $(selectElementId);

  options.forEach((option) => {
    const optionHtml = `<option value="${option}">${option}</option>`;
    $selectElement.append(optionHtml);
  });
}
