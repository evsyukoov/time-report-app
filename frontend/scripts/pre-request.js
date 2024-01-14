let departments = null
let employees = null
let projects = null

function preRequestAdmin() {
    preRequest('true')
}

function preRequestMain() {
    //Listeners
    // убрать всплывающий список по клику мимо списка
    document.addEventListener('click', (event) => {
      if (event.target.className !== 'dropbtn' && !event.target.className.startsWith('dropdown-block')) {
          if (isDropdownsActive()) {
              clearDropdowns()
          }
      }
    })
    preRequest('false')
}

function preRequest(flag) {
    if (departments == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-departments').then(resp => {
            departments = resp;
            console.log(departments)
        })
    }
    if (employees == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-employees?unused=' + flag).then(resp => {
            employees = resp;
            console.log(employees)
        })
    }
    if (projects == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-projects?unused=' + flag).then(resp => {
            projects = resp;
            console.log(projects)
        })
    }
}

