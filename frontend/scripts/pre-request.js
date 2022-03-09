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
        var departmentsDiv = document.getElementById("departments")
        var ul = document.createElement('ul');
        departmentsDiv.appendChild(ul)
        for (let i = 0; i < responseData.length; i++) {
            var li = document.createElement('li');
            li.innerHTML = responseData[i];
            ul.appendChild(li);
        }
        departmentsDiv.classList.toggle("show");

        window.onclick = function (event) {
            if (!event.target.matches('.dropdown-content')) {

                var dropdowns = document.getElementsByClassName("dropdown-content");
                for (var i = 0; i < dropdowns.length; i++) {
                    var openDropdown = dropdowns[i];
                    openDropdown.classList.remove('show');
                    while (openDropdown.firstChild) {
                        openDropdown.removeChild(openDropdown.firstChild);
                    }
                }
            }
        };
    })
}

const empl = () => {
    sendHttpRequest('GET', 'http://localhost:8080/report/get-departments').then(responseData => {
        employees = responseData;
    });
};

dep()
empl()

console.log(employees)
console.log(departments)

