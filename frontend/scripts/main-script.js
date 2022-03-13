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
    sendHttpRequest('GET', 'http://localhost:8082/report/get-departments').then(responseData => {
        var departmentsDiv = document.getElementById("departments")
        addElementsToUl(departmentsDiv, "dropdown-block-dep", responseData)
    })
}

const empl = () => {
    sendHttpRequest('GET', 'http://localhost:8082/report/get-employees').then(responseData => {
        var employeesDiv = document.getElementById("employees")
        addElementsToUl(employeesDiv, "dropdown-block-empl", responseData)
    })
}

function addElementsToUl(div, classElemName, responseData) {
    var ul;
    if (document.getElementById('ul-id' + classElemName) == null) {
        ul = createUl(classElemName)
    } else {
        deleteUl(classElemName)
        ul = createUl(classElemName)
    }
    div.appendChild(ul)
    for (let i = 0; i < responseData.length; i++) {
        var li = document.createElement('li');
        li.className = classElemName
        li.innerHTML = responseData[i];
        ul.appendChild(li);
    }

    window.onclick = function (event) {
        let elem = document.getElementById("ul-id" + classElemName)
        let li = event.target
        let ul = li.parentElement
        if (elem !== ul) {
            return;
        }
        if (li.matches("li"))  {
            deleteUl(classElemName)
            let ul = createUl(classElemName)
            ul.appendChild(li)
            div.appendChild(ul)
        } else if (!event.target.matches(classElemName)) {
            return
        } else {
            deleteUl(classElemName)
        }
    };
}

function deleteUl(classElemName) {
    let dropdowns = document.getElementsByClassName(classElemName);
    for (let i = 0; i < dropdowns.length; i++) {
        let openDropdown = dropdowns[i];
        while (openDropdown.firstChild) {
            openDropdown.removeChild(openDropdown.firstChild);
        }
    }
}

function createUl(classElemName) {
    let ul = document.createElement('ul');
    ul.id = 'ul-id' + classElemName
    return ul;
}


