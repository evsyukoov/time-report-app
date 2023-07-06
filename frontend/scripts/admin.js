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
        sendHttpRequest('GET', referer + '/report/get-employees?unused=true').then(resp => {
            employees = resp;
            console.log(employees)
        })
    }
    if (projects == null) {
        sendHttpRequest('GET', referer + '/report/get-projects?unused=true').then(resp => {
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

function addEmployee() {
    let position = document.getElementById("employee-position").value
    let fio = document.getElementById("employee-fio").value
    let selectList = document.getElementById("departments-admin");
    let department = selectList.options[selectList.selectedIndex].text
    if (!departments.includes(department)) {
        swal("Выберите отдел!",{
            icon: "error",
        });
        return;
    }
    if (position == null || position === "" || position.length < 5) {
        swal("Введите валидное поле должность!",{
            icon: "error",
        });
        return;
    }
    if (fio == null || fio === "") {
        swal("Поле ФИО не может быть пустым!",{
            icon: "error",
        });
        return;
    }
    let fioArr = fio.split(" ")
    if (fioArr.length !== 3) {
        swal("Введите Фамилию имя и отчество",{
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("POST", referer + "/admin/employee/add");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"name": fio, "position": position, "department": department});

     xhr.onreadystatechange = function(oEvent) {
         if (xhr.readyState === 4) {
            if (xhr.status === 200) {
             swal("Новый сотрудник успешно добавлен",{
                 icon: "info",
             });
         } else if (xhr.status === 400) {
            if (xhr.responseText.includes('ALREADY_CONTAINS')) {
                swal("Такой сотрудник уже есть в справочнике!",{
                                 icon: "error",
                            });
            } else {
                        swal("Проверьте введеные данные по сотруднику!",{
                             icon: "error",
                        });
            }
         } else if (xhr.status === 500) {
             swal("Произошла непредвиденная ошибка на сервере, сообщите администратору",{
                 icon: "error",
             });
         }
         }
     };
    xhr.send(data)
}

function addProject() {
    let project = document.getElementById("project-input").value
    if (project == null || project === "" || project.length < 5) {
        swal("Введите валидное поле проект!",{
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("POST", referer + "/admin/project/add");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"projectName": project});

     xhr.onreadystatechange = function(oEvent) {
         if (xhr.readyState === 4) {
            if (xhr.status === 200) {
             swal("Новый проект успешно добавлен",{
                 icon: "info",
             });
         } else if (xhr.status === 400) {
            if (xhr.responseText.includes('ALREADY_CONTAINS')) {
                swal("Такой проект уже есть в справочнике!",{
                                 icon: "error",
                            });
            } else {
                        swal("Проверьте введеные данные по проекту!",{
                             icon: "error",
                        });
            }
         } else if (xhr.status === 500) {
             swal("Произошла непредвиденная ошибка на сервере, сообщите администратору",{
                 icon: "error",
             });
         }
         }
     };
    xhr.send(data)
}


