let departments = null
let employees = null
let projects = null

function preRequestAdmin() {
    preRequest()
}

function preRequestMain() {
    console.log('referer: ' + document.referrer)
    //Listeners
    // убрать всплывающий список по клику мимо списка
    document.addEventListener('click', (event) => {
      if (event.target.className !== 'dropbtn' && !event.target.className.startsWith('dropdown-block')) {
          if (isDropdownsActive()) {
              clearDropdowns()
          }
      }
    })
    preRequest()
}

function preRequest() {
    if (departments == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-departments').then(resp => {
            departments = resp;
            console.log(departments)
        })
    }
    if (employees == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-employees').then(resp => {
            employees = resp;
            console.log(employees)
        })
    }
    if (projects == null) {
        sendHttpRequest('GET', referer + '/time-report-app/report/get-projects').then(resp => {
            projects = resp;
            console.log(projects)
        })
    }
}

