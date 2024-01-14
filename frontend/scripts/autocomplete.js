//будем получать с сервера список по вхождению word
//на сервере список кешируем, обновляя кеш, чтобы не ходить постоянно в БД
function getDataByInput(path) {

    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("POST", referer + "/time-report-app/autocomplete/" + path);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.setRequestHeader('Access-Control-Allow-Origin', referer)
        xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')

        let word = document.getElementById(path).value
        console.log(word)

        var data = JSON.stringify({
            "query": word
        });

        xhr.responseType = "json"

        xhr.onload = function (oEvent) {
                if (xhr.status !== 200) {
                    reject('Something went wrong!');
                    console.log('Смотреть логи на сервере')
                    return
                }
                resolve(xhr.response);
        };

        xhr.onerror = () => {
            reject('Something went wrong!');
        };

        xhr.send(data)
    })
}