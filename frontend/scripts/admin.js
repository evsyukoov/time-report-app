let departments = null
let employees = null
let projects = null

function preRequest() {
    if (departments == null) {
        sendHttpRequest('GET', referer + '/report/get-departments').then(resp => {
            departments = resp;
            console.log(departments)
        })
    }
    if (employees == null) {
        sendHttpRequest('GET', referer + '/report/get-employees').then(resp => {
            employees = resp;
            console.log(employees)
        })
    }
    if (projects == null) {
        sendHttpRequest('GET', referer + '/report/get-projects').then(resp => {
            projects = resp;
            console.log(projects)
        })
    }
}

function fillSelectSections(idElem) {
    const select = document.getElementById(idElem)
    let optionSize = select.length
    if (optionSize > 1) {
        return
    }
    let filledArray
    if (idElem.startsWith('employees')) {
        filledArray = employees
    } else if (idElem.startsWith('departments')) {
        filledArray = departments
    } else {
        filledArray = projects
    }
    for (let i = 0; i < filledArray.length; i++) {
        let option = document.createElement('option')
        option.value = i + ''
        option.innerHTML = filledArray[i]
        option.text = filledArray[i]
        select.appendChild(option)
    }
}


