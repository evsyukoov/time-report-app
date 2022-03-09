var departments;
var employees;

const sendHttpRequest = (method, url) => {
    const promise = new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url);

        xhr.responseType = 'json';

        xhr.setRequestHeader('Content-Type', 'application/json');

        xhr.onload = () => {
            if (xhr.status >= 400) {
                reject(xhr.response);
            } else {
                resolve(xhr.response);
            }
        };

        xhr.onerror = () => {
            reject('Something went wrong!');
        };

        xhr.send();
    });
    return promise;
};

const dep = () => {
    sendHttpRequest('GET', 'http://localhost:8080/report/get-departments').then(responseData => {
        departments = responseData;
    });
};

const empl = () => {
    sendHttpRequest('GET', 'http://localhost:8080/report/get-departments').then(responseData => {
        employees = responseData;
    });
};

dep()
empl()

console.log(employees)
console.log(departments)

