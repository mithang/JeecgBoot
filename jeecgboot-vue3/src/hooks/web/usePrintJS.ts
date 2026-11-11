import { nextTick } from 'vue';
import $printJS, { Configuration } from 'print-js';
import Print from 'vue-print-nb-jeecg/src/printarea';

/**
 * Gọi printJS, nếutype = html, thì đi printNB phương thức
 */
export function printJS(configuration: Configuration) {
  if (configuration?.type === 'html') {
    printNb(configuration.printable);
  } else {
    return $printJS(configuration);
  }
}

/** Gọi printNB In */
export function printNb(domId, endCallback?) {
  if (domId) {
    localPrint(domId, endCallback);
  } else {
    window.print();
    endCallback && endCallback();
  }
}

let closeBtn = true;

function localPrint(domId, endCallback) {
  if (typeof domId === 'string' && !domId.startsWith('#')) {
    domId = '#' + domId;
  }
  nextTick(() => {
    if (closeBtn) {
      closeBtn = false;
      new Print({
        el: domId,
        endCallback() {
          closeBtn = true;
          endCallback && endCallback();
        },
      });
    }
  });
}
