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

const dep = async () => {
    let currentDepartments
    await getDataByInput('get-departments').then((resolve) => {
        currentDepartments = resolve;
        console.log(departments)
        return currentDepartments
    })
    var departmentsDiv = document.getElementById("departments")
    addElementsToUl(departmentsDiv, "dropdown-block-dep", currentDepartments, true)
}

const empl = async () => {
    let currentEmployees
        await getDataByInput('get-employees').then((resolve) => {
            currentEmployees = resolve;
            return currentEmployees
        })
        var employeesDiv = document.getElementById("employees")
        addElementsToUl(employeesDiv, "dropdown-block-empl", currentEmployees, false)
        if (isValidInputValue("get-employees", employees)) {
            document.getElementById("empl-report").disabled = false
            document.getElementById("empl-report").checked = false
        } else {
            document.getElementById("empl-report").disabled = true
            document.getElementById("empl-report").checked = false
        }
}

const project = async () => {
    let currentProjects
        await getDataByInput('get-projects').then((resolve) => {
            currentProjects = resolve;
            return currentProjects
        })
        var projectsDiv = document.getElementById("projects")
        addElementsToUl(projectsDiv, "dropdown-block-project", currentProjects, false)
        if (isValidInputValue("get-projects", projects)) {
            document.getElementById("project-report").disabled = false
            document.getElementById("project-report").checked = false
        } else {
            document.getElementById("project-report").disabled = true
            document.getElementById("project-report").checked = false
        }
}

function isValidInputValue(inputId, checkLst) {
    return document.getElementById(inputId).value != null && document.getElementById(inputId).value !== ""
    && checkLst.includes(document.getElementById(inputId).value)
}

function addElementsToUl(div, classElemName, data, isDepartments) {
    deleteUl(classElemName)
    let ul = createUl(classElemName)
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
            let inputFormClass
            if (userChoiceLi.className === "dropdown-block-dep") {
                inputFormClass = "get-departments"
            } else if(userChoiceLi.className === "dropdown-block-empl") {
                inputFormClass = "get-employees"
            } else {
                inputFormClass = "get-projects"
            }
            deleteUl(classElemName)
            document.getElementById(inputFormClass).value = userChoiceLi.textContent
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

function clearInput(classElemName) {
    deleteUl(classElemName)
    //document.getElementById(inputId).value = ""
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

function isDropdownsActive() {
    return isDropdownActive("dropdown-block-dep") ||
            isDropdownActive("dropdown-block-empl") ||
            isDropdownActive("dropdown-block-project")
}

function clearDropdowns() {
    clearInput("dropdown-block-dep")
    clearInput("dropdown-block-empl")
    clearInput("dropdown-block-project")
}

function isDropdownActive(classElemName) {
    let dropdowns = document.getElementsByClassName(classElemName);
    for (let i = 0; i < dropdowns.length; i++) {
        let openDropdown = dropdowns[i];
        if (openDropdown.firstChild != null) {
            return true
        }
    }
    return false;
}

function createUl(classElemName) {
    let ul = document.createElement('ul');
    ul.id = 'ul-id-' + classElemName
    return ul;
}


