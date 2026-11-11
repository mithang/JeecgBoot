export const useCodeHinting = (CodeMirror, keywords, language) => {
  const currentKeywords: any = [...keywords];
  const codeHintingMount = (coder) => {
    if (keywords.length) {
      coder.setOption('mode', language);
      setTimeout(() => {
        coder!.on('cursorActivity', function () {
          coder?.showHint({
            completeSingle: false,
            // container: containerRef.value
          });
        });
      }, 1e3);
    }
  };

  const codeHintingRegistry = () => {
    // Từ khóa tùy chỉnh(.cấp trên)
    const customKeywords: string[] = [];

    currentKeywords.forEach((item) => {
      if (item.superiors) {
        customKeywords.push(item.superiors);
      }
    });
    const funcsHint = (cm, callback) => {
      // Lấy vị trí con trỏ
      const cur = cm.getCursor();
      // Lấy thông tin của từ hiện tại
      const token = cm.getTokenAt(cur);
      const start = token.start;
      const end = cur.ch;
      const str = token.string;
      let recordKeyword = null;
      console.log('Vị trí con trỏ:', cur, 'Thông tin từ:', token, `start:${start},end:${end},str:${str}`);

      if (str.length) {
        if (str === '.') {
          // Tìm.Phía trước có từ khóa đã định nghĩa không
          const curLineCode = cm.getLine(cur.line);
          for (let i = 0, len = customKeywords.length; i < len; i++) {
            const k = curLineCode.slice(-(customKeywords[i].length + 1), -1);
            if (customKeywords.includes(k)) {
              recordKeyword = k;
              break;
            }
          }
        } else {
          // Tìm xem phía trước từ có.this(.Từ khóa)
          const curLineCode = cm.getLine(cur.line);
          for (let i = 0, len = customKeywords.length; i < len; i++) {
            const k = curLineCode.slice(start - (customKeywords[i].length + 1), start);
            if (k.substr(-1) === '.' && customKeywords.includes(k.replace('.', ''))) {
              recordKeyword = k.replace('.', '');
              break;
            }
          }
        }
        const findIdx = (a, b) => a.toLowerCase().indexOf(b.toLowerCase());
        let list = currentKeywords.filter((item) => {
          if (recordKeyword) {
            // Tra thuộc tính hoặc phương thức của đối tượng cụ thể
            return item.superiors === recordKeyword;
          } else {
            // Tra cứu thuộc tính hoặc phương thức toàn cục
            return item.superiors == undefined;
          }
        });
        if (str === '.') {
          if (recordKeyword == null) {
            list = [];
          }
        } else {
          list = list
            .filter((item) => {
              const { text } = item;
              const index = findIdx(text, str);
              let result = text.startsWith('.') ? index === 1 : index === 0;
              return result;
            })
            .sort((a, b) => {
              if (findIdx(a.text, str) < findIdx(b.text, str)) {
                return -1;
              } else {
                return 1;
              }
            });
        }

        if (list.length === 1) {
          // Khi chỉ còn một cái thì có thể là tự nhập, nhập đến cuối cần bỏ gợi ý.
          const item = list[0];
          if (item.text === str || item.text.substring(1) === str) {
            list = [];
          }
        }
        if (list.length) {
          // Khi str không phải là dấu chấm thì bỏ dấu chấm
          if (str != '.') {
            list = list.map((item) => {
              if (item.text.indexOf('.') === 0) {
                return { ...item, text: item.text.substring(1) };
              }
              return item;
            });
          }
          callback({
            list: list,
            from: CodeMirror.Pos(cur.line, start),
            to: CodeMirror.Pos(cur.line, end),
          });
          // Giải thích logic mã: 【QQYUN-8865] js tăng cường thêm nhắc nhở khi di chuột vào
          const item = currentKeywords[0];
          if (item?.desc) {
            setTimeout(() => {
              const elem: HTMLUListElement = document.querySelector('.CodeMirror-hints')!;
              if (elem) {
                const childElems = elem.children;
                Array.from(childElems).forEach((item) => {
                  const displayText = item.textContent;
                  const findItem = currentKeywords.find((item) => item.displayText === displayText);
                  if (findItem) {
                    item.setAttribute('title', findItem.desc);
                  }
                });
              }
            }, 0);
          }
        } else {
        }
      }
    };
    funcsHint.async = true;
    funcsHint.supportsSelection = true;
    // Tự động hoàn thành
    keywords.length && CodeMirror.registerHelper('hint', language, funcsHint);
  };
  return {
    codeHintingRegistry,
    codeHintingMount,
  };
};
