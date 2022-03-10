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
        addElementsToUl(departmentsDiv, "dropdown-block", responseData)
    })
}

const empl = () => {
    sendHttpRequest('GET', 'http://localhost:8080/report/get-employees').then(responseData => {
        var employeesDiv = document.getElementById("employees")
        addElementsToUl(employeesDiv, "dropdown-block", responseData)
    })
}

function addElementsToUl(div, classElemName, responseData) {
    var ul;
    if (document.getElementById('ul-id') == null) {
        ul = document.createElement('ul');
        ul.id = 'ul-id'
    } else {
        ul = document.getElementById('ul-id')
    }
    div.appendChild(ul)
    for (let i = 0; i < responseData.length; i++) {
        var li = document.createElement('li');
        li.innerHTML = responseData[i];
        ul.appendChild(li);
    }

    window.onclick = function (event) {
        let li = event.target;
        console.log(li)
        if (event.target.matches("li")) {
            deleteUlExcept(li, classElemName)
        } else {
            //deleteUlExcept(null, classElemName)
        }
    };
}

function deleteUlExcept(liElem, classElemName) {
    let ul = document.getElementById('ul-id')
    console.log(ul.childNodes.length)
    ul.childNodes.forEach(li => {
        if (li.textContent !== liElem.innerHTML) {
            li.remove()
        }
    })
}


