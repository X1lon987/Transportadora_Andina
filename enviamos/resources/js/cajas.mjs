export default class Cajas {
    static #table
    static #modal
    static #currentOption
    static #form
    static #tipoEstado = []
    static #remitente
    static #destinatario

    constructor() {
        throw new Error('No requiere instancias, todos los métodos son estáticos. Use Cajas.init()')
    }

    static async init() {
        try {
            Cajas.#form = await Helpers.fetchText('./resources/html/Cajas.html') //Aqui estamos cargando un formulario externo
            // intentar cargar los datos de los usuarios
            let response = await Helpers.fetchJSON(`${urlAPI}/cliente`)  //Aqui cargamos la informacion de los clientes
            if (response.message != 'ok') {
                throw new Exception(response) // JavaScript no cuenta con una clase Exception
            }

            // crear las opciones para un select de clientes
            //si la informacion de los clientes llega sin problemas usamos este metodo que se encarga de crear unas opciones especiales para los clientes que se utilizaran mas adelante
            Cajas.#remitente = Helpers.toOptionList({
                items: response.data,
                value: 'id',
                text: 'nombre',
                firstOption: 'Seleccione cliente',
            })
            Cajas.#destinatario = Helpers.toOptionList({
                items: response.data,
                value: 'id',
                text: 'nombre',
                firstOption: 'Seleccione un Cliente '
            })

            //Validacion de la carga de Cajas 
            response = await Helpers.fetchJSON(`${urlAPI}/caja`)
            console.log(response)
            if (response.message != 'ok') {
                throw new Error(response.message)
            }
            //cargamos los estados del envio en general
            Cajas.#tipoEstado = (await Helpers.fetchJSON(`${urlAPI}/envio/estados`)).data

            // agregar al <main> de index.html la capa que contendrá la tabla
            document.querySelector('main').innerHTML = `
            <div class="p-2 w-full">
                <div id="table-container" class="m-2"></div>
            </dv>`

            Cajas.#table = new Tabulator('#table-container', {
                height: tableHeight, // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
                data: response.data, //Aqui se carga la informacion de los Cajas
                layout: 'fitColumns', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
                columns: [
                    // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
                    { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Cajas.#editRowClick },
                    { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Cajas.#deleteRowClick },
                    { title: 'Tipo Envio', field: 'tipo', visible: false },
                    { title: 'Num Guia', field: 'id', hozAlign: 'center', width: 120 },
                    { title: 'Dice contener', field: 'contenido', width: 200 },
                    { title: 'Peso (kg)', field: 'peso', hozAlign: 'center', width: 100 },
                    { title: 'Vol.m³', field: 'volumen', hozAlign: 'center', width: 100, formatterParams: { precision: 0 } },
                    { title: 'Fragil', field: 'fragil', hozAlign: 'center', formatter: 'tickCross', width: 80 },
                    { title: 'Remitente', field: 'remitente.nombre', width: 150 },
                    { title: 'Destinatario', field: 'destinatario.nombre', width: 150 },
                    { title: 'Valor Declarado', field: 'valorEstimado', hozAlign: 'center', width: 160, formatter: 'money', formatterParams: { precision: 0 } },
                    { title: 'Costo', field: 'costo', hozAlign: 'center', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
                    {
                        title: 'Estado Actual',
                        field: 'estados',
                        width: 200,
                        formatter: function (cell) {
                            const lista = cell.getValue()  //Aqui samos el valor que esta almacenado en la lista del los estados del paquete
                            const ultimo = lista[lista.length - 1]  //Aqui guardamos el indice del ultimo estado 
                            console.log(ultimo)
                            const estado = Cajas.#tipoEstado.find(item => item.key === ultimo.estado)
                            return `${DateTime.fromISO(ultimo.fechayHora).toFormat('yy-MM-dd hh:mm')} - ${estado.value}`//Aqui estamos mandando el formato en el cual se va a mostrar la columna de estados envio dentro de la web 
                        }
                    },
                ],
                responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
                initialSort: [
                    // establecer el ordenamiento inicial de los datos
                    { column: 'estados', dir: 'asc' },
                ],
                columnDefaults: {
                    tooltip: true, //show tool tips on cells
                },
                // mostrar al final de la tabla un botón para agregar registros
                footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
            })
            Cajas.#table.on('tableBuilt', () =>
                document.querySelector('#add-row').addEventListener('click', Cajas.#addRow)
            )
        } catch (e) {
            Toast.show({ title: 'Cajas', message: 'Falló la carga de la información', mode: 'danger', error: e })
        }

        return this
    }

    static async #addRow() {
        Cajas.#currentOption = 'add'
        Cajas.#modal = new Modal({  //Crea una instancia de un objeto Popup el cual nos permite crear ventanas adyacentes o desplegables en nuestra pagina web
            classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
            //classes: es una propiedad del objeto modal que acabamos de crear que le da estilos al bootstrap
            title: `<h5 class="text-secondary strong">Ingreso de Cajas</h5>`,                //Titulo del cuadro de dialogo donde se ingresaran las Cajas
            content: Cajas.#form,  //content: aqui se almacena el cuerpo de la ventana modal donde se van a ingresar las Cajas
            buttons: [                   //recibe un array en que va a ir implementada la funcion de lso botones que van a aparecer dentro de la ventana modal
                { caption: addButton, classes: 'btn btn-primary me-2', action: () => Cajas.#add() },  //Utilizamos un callBack para no estar cargando siempre una fumcion que solo vamos a utilizar cuando se abra la ventana modal
                { caption: cancelButton, classes: 'btn btn-secondary', action: () => Cajas.#modal.close() }   //este boton va a tener la funcion  de cerrar la ventana modal
            ],
            doSomething: Cajas.#displayDataOnForm  //esta instruccion nos muestra la ventana modal que acabamos de personalizar
        })
        Cajas.#modal.show()
    }

    static #editRowClick = async (e, cell) => { //cell nos referencia sobre la fila en la que estamos trabajando o sobre la cual estamos ubicados 
        Cajas.#currentOption = 'edit'
        console.log(cell.getRow().getData())
        //Vamos a implementar una nueva ventana modal para poder ingresar los combios que queremos hacer para las Cajas

        Cajas.#modal = new Modal({
            classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
            title: `<h5 class="text-secondary">Actualizacion de Cajas</h5>`,
            content: Cajas.#form,
            buttons: [
                { caption: editButton, classes: 'btn btn-primary me-2', action: () => Cajas.#edit(cell) },   //como estre metodo toma una cell especifica que es aquella que se va a editar por eso la referenciamos dentro de la funcion edit
                { caption: cancelButton, classes: 'btn btn-secondary', action: () => Cajas.#modal.close() },
            ],
            doSomething: (idModal) => Cajas.#displayDataOnForm(idModal, cell.getRow().getData()), //este llamado al metodo le enviamos el idModal y la informacion almacenada dentro de la fila en la que se esta situado mediante getRow()
        })
        Cajas.#modal.show()
    }

    static #deleteRowClick = async (e, cell) => {
        Cajas.#currentOption = 'delete'
        console.log(cell.getRow().getData())
        //Mismo proceso, la idea es mostrar un cuadro de dialogo
        Cajas.#modal = new Modal({
            classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
            title: `<h5 class="text-secondary">Eliminar Cajas</h5>`,
            content: `<span class= "text-back dark:text-gray-300">
                      Confirme la eliminacion del envio: <br>
                      Id:${cell.getRow().getData().nroGuia} - Contenido:${cell.getRow().getData().contenido}<br>         
                      Peso : ${cell.getRow().getData().peso}<br>
                      Fragil : ${cell.getRow().getData().fragil}<br>
                      Destinatario: ${cell.getRow().getData().destinatario.nombre}<br>
                      Remitente : ${cell.getRow().getData().remitente.nombre}<br>
                      Valor Declarado : ${cell.getRow().getData().valorEstimado}<br>
                      Costo : ${cell.getRow().getData().costo}<br>
                      
                      </span>`,//en content obtuvimos la informacion de la caja mediante el cell para mostrar en el cuadro de dialogo sobre la caja a eliminar
            buttons: [ //preguntar como hacer que aparezca el estado actual del envio
                { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Cajas.#delete(cell) },
                { caption: cancelButton, classes: 'btn btn-secondary', action: () => Cajas.#modal.close() },
            ],
        })
        Cajas.#modal.show()
    }

    //agregar nuevas Cajas(creacion y personalizacion de una ventana modal al presionar  nuevo registro)
    static async #add() {//proximamnete este metodo se encargara de hacer peticiones tipo post
        try {
            if (!Helpers.okForm('#form-cajas', Cajas.#otherValidations)) {
                return
            }
            const body = Cajas.#getFormData()

            let response = await Helpers.fetchJSON(`${urlAPI}/caja`, {
                method: 'POST',
                body,
            })
            if (response.message === 'ok') {
                Cajas.#table.addRow(response.data)
                Cajas.#modal.remove()
                Toast.show({ message: 'Agregado exitosamente' })
            } else {
                Toast.show({ message: 'no se pudo agregar el registro', mode: 'danger', error: response })
            }

        } catch (e) {
            Toast.show({ message: 'Fallo la operacion de la creacion del registro', mode: 'danger', error: e })
        }
    }
    static async #edit(cell) {//Proximamente este metodo se encargara de hacer las peticiones patch
        try {
            if (!Helpers.okForm('#form-cajas', Cajas.#otherValidations)) {
                return
            }
            const body = Cajas.#getFormData()
            const url = `${urlAPI}/caja/${cell.getRow().getData().nroGuia}`   //Aqui estamos guardando la direccion o end point especifico de la Cajas que deseamos editar

            let response = await Helpers.fetchJSON(url, {
                method: 'PATCH',
                body,
            })
            if (response.message === 'ok') {
                Toast.show({ message: 'la caja  fue actualizada exitosamente!' })
                cell.getRow().update(response.data)
                Cajas.#modal.remove()
            }
            else {
                Toast.show({ message: 'No se pudo actualizar la Cajas', mode: 'danger', error: response })
            }
        } catch (e) {
            Toast.show({ message: 'Problemas al actualizar Cajas (en el try)', mode: 'danger', error: e })
        }
    }

    static async #delete(cell) {//Proximamente este metodo se encargara de hacer la peticion delete 
        try {

            const url = `${urlAPI}/caja/${cell.getRow().getData().id}`

            let response = await Helpers.fetchJSON(url, {
                method: 'DELETE',
                //Al borrar no necesitamos cargas de ningun tipo, por eso este metodo no contiene body
            })
            if (response.message === 'ok') {
                Toast.show({ message: 'la caja ha sido eliminada de manera exitosa!' })
                cell.getRow().delete()
                Cajas.#modal.close()
            } else {
                Toast.show({ message: 'No se pudo eliminar la caja', mode: 'danger', error: response })
            }

        } catch (e) {
            Toast.show({ message: 'Problemas al eliminar la caja (try)', mode: 'danger', error: e })
        }
    }

//DisplayDataOnForm se encarga de mostrar la informacion de el envio que esta en la fila seleccionada al momento de querer editar un envio
    static #displayDataOnForm(idModal, rowData) {//Este metodo se encarga de mostrar y editar el registro de Cajas de la ventana modal
        // referenciar el select "cliente"
        const selectRemitente = document.querySelector(`#${idModal} #remitente`)
        const selectDestinatario = document.querySelector(`#${idModal} #destinatario`)

        // asignar la lista de opciones al select "cliente" de Cajas.html
        selectRemitente.innerHTML = Cajas.#remitente
        selectDestinatario.innerHTML = Cajas.#destinatario

        if (Cajas.#currentOption === 'edit') {
            //Mostrar todos los datos de la fila actual en el formulario
            document.querySelector(`#${idModal} #id`).value = rowData.id
            document.querySelector(`#${idModal} #contenido`).value = rowData.contenido
            document.querySelector(`#${idModal} #alto`).value = rowData.alto
            document.querySelector(`#${idModal} #ancho`).value = rowData.ancho
            document.querySelector(`#${idModal} #largo`).value = rowData.largo
            document.querySelector(`#${idModal} #peso`).value = rowData.peso
            document.querySelector(`#${idModal} #valorEstimado`).value = rowData.valorEstimado
            selectRemitente.value = rowData.remitente.id
            selectDestinatario.value = rowData.destinatario.id
        }
    }
    static #getFormData() {
        return {
            nroGuia: document.querySelector(`#${Cajas.#modal.id} #id`).value,
            remitente: document.querySelector(`#${Cajas.#modal.id} #remitente`).value,
            destinatario: document.querySelector(`#${Cajas.#modal.id} #destinatario`).value,
            contenido: document.querySelector(`#${Cajas.#modal.id} #contenido`).value,
            fragil: document.querySelector(`#${Cajas.#modal.id} #fragil`).checked,
            alto: parseFloat(document.querySelector(`#${Cajas.#modal.id} #alto`).value),
            ancho: parseFloat(document.querySelector(`#${Cajas.#modal.id} #ancho`).value),
            largo: parseFloat(document.querySelector(`#${Cajas.#modal.id} #largo`).value),
            peso: parseFloat(document.querySelector(`#${Cajas.#modal.id} #peso`).value),
            valorEstimado: parseFloat(document.querySelector(`#${Cajas.#modal.id} #valorEstimado`).value)
        }
    }
    static #otherValidations(idModal) {
        let respuesta = true
        const selectRemitente = document.querySelector(`#${Cajas.#modal.id} #remitente`)
        const selectDestinatario = document.querySelector(`#${Cajas.#modal.id} #destinatario`)

        if (selectRemitente.value === '') {
            Toast.show({ message: 'Falta seleccionar un remitente', mode: 'warning' })
            respuesta = false
        }

        if (selectDestinatario.value === '') {
            Toast.show({ message: 'Falta seleccionar un destinatario', mode: 'warning' })
            respuesta = false
        }

        if (selectRemitente.value === selectDestinatario.value) {
            Toast.show({ message: 'El destinatario debe ser distinto al remitente', mode: 'warning' })
            respuesta = false
        }
        return respuesta
    }
}
