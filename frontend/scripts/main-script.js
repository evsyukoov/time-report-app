let referer = document.referrer

const sendHttpRequest = (method, url) => {
    return new Promise((resolve, reject) => {
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
};

const dep = () => {
    sendHttpRequest('GET', referer + '/report/get-departments').then(responseData => {
        startConditionCheckboxes()
        var departmentsDiv = document.getElementById("departments")
        addElementsToUl(departmentsDiv, "dropdown-block-dep", responseData)
    })
}

const empl = () => {
    sendHttpRequest('GET', referer + '/report/get-employees').then(responseData => {
        startConditionCheckboxes()
        var employeesDiv = document.getElementById("employees")
        addElementsToUl(employeesDiv, "dropdown-block-empl", responseData)
    })
}

function startConditionCheckboxes() {
    document.getElementById("empl-report").setAttribute("disabled", "disabled");
    document.getElementById("empl-report").checked = false
}

function addElementsToUl(div, classElemName, responseData) {
    var ul;
    if (document.getElementById('ul-id-' + classElemName) == null) {
        ul = createUl(classElemName)
    } else {
        deleteUl(classElemName)
    }
    div.appendChild(ul)
    for (let i = 0; i < responseData.length; i++) {
        var li = document.createElement('li');
        li.className = classElemName
        li.innerHTML = responseData[i];
        li.addEventListener('click', function (event) {
            let userChoiceLi = event.target;
            deleteUl(classElemName)
            let ul = createUl(classElemName)
            ul.appendChild(userChoiceLi)
            div.appendChild(ul)
            if (classElemName === "dropdown-block-empl") {
                document.getElementById("empl-report").removeAttribute("disabled")
                document.getElementById("empl-report").checked = false
            }

        })
        ul.appendChild(li);
    }
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
    ul.id = 'ul-id-' + classElemName
    return ul;
}


