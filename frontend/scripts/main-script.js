let referer = document.referrer
let index = referer.indexOf("/")
if (index !== -1) {
    referer = referer.substring(0, index)
}

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
        var departmentsDiv = document.getElementById("departments")
        addElementsToUl(departmentsDiv, "dropdown-block-dep", departments, true)
        startConditionCheckboxes()
}

const empl = () => {
        var employeesDiv = document.getElementById("employees")
        addElementsToUl(employeesDiv, "dropdown-block-empl", employees, false)
        startConditionCheckboxes()
}

const project = () => {
        var employeesDiv = document.getElementById("projects")
        addElementsToUl(employeesDiv, "dropdown-block-project", projects, false)
        startConditionCheckboxes()
}

function startConditionCheckboxes() {
    if (document.getElementById("ul-id-dropdown-block-project") != null) {
        document.getElementById("empl-report").setAttribute("disabled", "disabled");
        document.getElementById("empl-report").checked = false
    }
    if (document.getElementById("ul-id-dropdown-block-project") == null) {
        document.getElementById("project-report").setAttribute("disabled", "disabled");
        document.getElementById("project-report").checked = false
    }
}

function addElementsToUl(div, classElemName, data, isDepartments) {
    var ul = null;
    if (document.getElementById('ul-id-' + classElemName) == null) {
        ul = createUl(classElemName)
    } else {
        deleteUl(classElemName)
    }
    if (ul == null) {
        return
    }
    div.appendChild(ul)
    for (let i = 0; i < data.length; i++) {
        var li = document.createElement('li');
        li.className = classElemName
        if (isDepartments) {
            li.innerHTML = data[i]["name"];
        } else {
            li.innerHTML = data[i]
        }
        li.addEventListener('click', function (event) {
            let userChoiceLi = event.target;
            deleteUl(classElemName)
            let ul = createUl(classElemName)
            ul.appendChild(userChoiceLi)
            div.appendChild(ul)
            if ((classElemName === "dropdown-block-empl" && document.getElementById("ul-id-dropdown-block-project") == null)) {
                document.getElementById("empl-report").removeAttribute("disabled")
                document.getElementById("empl-report").checked = false
            }
            if (classElemName === "dropdown-block-project") {
                document.getElementById("project-report").removeAttribute("disabled")
                document.getElementById("project-report").checked = false
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


