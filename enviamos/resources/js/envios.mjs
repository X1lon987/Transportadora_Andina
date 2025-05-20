
export default class Envios {
  static #table
  static #modal
  static #currentOption
  static #form
  static #tipoEstado = []
  static #mode
  static #remitente
  static #destinatario

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Envios.init()')
  }

  static async init(mode = '') {
    Envios.#mode = mode
    try {
      //cargamos los estados del envio en general
      Envios.#tipoEstado = (await Helpers.fetchJSON(`${urlAPI}/envio/estados`)).data
      Envios.#form = await Helpers.fetchText(`./resources/html/envios.html`)

      // intentar cargar los datos de cada cliente
      let response = await Helpers.fetchJSON(`${urlAPI}/cliente`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      Envios.#remitente = Helpers.toOptionList({
        items: response.data,
        value: 'id',
        text: 'nombre',
        firstOption: 'Seleccione Cliente'
      })
      Envios.#destinatario = Helpers.toOptionList({
        items: response.data,
        value: 'id',
        text: 'nombre',
        firstOption: 'Seleccione Un Cliente'
      })
      //vbalidacion de la carga del envio
      response = await Helpers.fetchJSON(`${urlAPI}/${mode}`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
        <div class="p-2 w-full">
            <div id="table-container" class="m-2"></div>
        </dv>`

      Envios.#table = new Tabulator('#table-container', {
        height: tableHeight, // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data, //Aqui se carga la informacion de los Envios
        layout: 'fitColumns', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Envios.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Envios.#deleteRowClick },
          { title: 'Tipo Envio', field: 'tipo', visible: false },
          { title: 'Num Guia', field: 'id', hozAlign: 'center', width: 120 },
          { title: 'Dice contener', field: 'contenido', width: 250 },
          { title: 'Peso (kg)', field: 'peso', hozAlign: 'center', width: 100, visible: mode !== 'sobre' },
          { title: 'Certificado', field: 'certificado', hozAlign: 'center', formatter: 'tickCross', width: 80, visible: mode === 'sobre' },
          { title: 'Fragil', field: 'fragil', hozAlign: 'center', formatter: 'tickCross', width: 80, visible: mode !== 'sobre' },
          { title: 'Remitente', field: 'remitente.nombre', width: 150 },
          { title: 'Destinatario', field: 'destinatario.nombre', width: 150 },
          { title: 'Valor Declarado', field: 'valorEstimado', hozAlign: 'center', width: 160, formatter: 'money', formatterParams: { precision: 0 }, visible: mode !== 'sobre' },
          { title: 'Costo', field: 'costo', hozAlign: 'center', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
          {
            title: 'Estado Actual',
            field: 'estados',
            width: 200,
            formatter: function (cell) {
              const lista = cell.getValue()  //Aqui samos el valor que esta almacenado en la lista del los estados del paquete

              const ultimo = lista[lista.length - 1]  //Aqui guardamos el indice del ultimo estado 

              const estado = Envios.#tipoEstado.find(item => item.key === ultimo.estado)
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
      Envios.#table.on('tableBuilt', () =>
        document.querySelector('#add-row').addEventListener('click', Envios.#addRow)
      )
    } catch (e) {
      Toast.show({ title: 'Envios', message: 'Falló la carga de la información', mode: 'danger', error: e })
    }
    return this
  }

  static #editRowClick = async (e, cell) => {
    Envios.#currentOption = 'edit'
    console.log(cell.getRow().getData())
    //Vamos a implementar una nueva ventana modal para poder ingresar los combios que queremos hacer para las Envios

    Envios.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class="text-secondary">Actualizacion de Envios</h5>`,
      content: Envios.#form,
      buttons: [
        { caption: editButton, classes: 'btn btn-primary me-2', action: () => Envios.#edit(cell) },   //como estre metodo toma una cell especifica que es aquella que se va a editar por eso la referenciamos dentro de la funcion edit
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Envios.#modal.remove() },
      ],
      doSomething: (idModal) => Envios.#displayDataOnForm(idModal, cell.getRow().getData()), //este llamado al metodo le enviamos el idModal y la informacion almacenada dentro de la fila en la que se esta situado mediante getRow()
    })
    Envios.#modal.show()
  }

  static #deleteRowClick = async (e, cell) => {
    Envios.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    //Mismo proceso, la idea es mostrar un cuadro de dialogo
    Envios.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class="text-secondary">--Eliminar Envios--</h5>`,
      content: `<span class= "text-back dark:text-gray-300">
                  Confirme la eliminacion de la mercancia: <br>
                  Id:${cell.getRow().getData().nroGuia} - Contenido:${cell.getRow().getData().contenido}<br>         
                  Peso : ${cell.getRow().getData().peso}<br>
                  Fragil : ${cell.getRow().getData().fragil}<br>
                  Destinatario: ${cell.getRow().getData().destinatario.nombre}<br>
                  Remitente : ${cell.getRow().getData().remitente.nombre}<br>
                  Valor Declarado : ${cell.getRow().getData().valorEstimado}<br>
                  Costo : ${cell.getRow().getData().costo}<br>
                  </span>`,//en content obtuvimos la informacion de la mercancia mediante el cell para mostrar en el cuadro de dialogo sobre la mercancia a eliminar
      buttons: [ //preguntar como hacer que aparezca el estado actual del envio
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Envios.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Envios.#modal.close() },
      ],
    })
    Envios.#modal.show()
  }

  static async #addRow() {
    Envios.#currentOption = 'add'
    Envios.#modal = new Modal({  //Crea una instancia de un objeto Popup el cual nos permite crear ventanas adyacentes o desplegables en nuestra pagina web
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      //classes: es una propiedad del objeto modal que acabamos de crear que le da estilos al bootstrap
      title: `<h5 class="text-secondary">Ingreso de Envios</h5>`,                //Titulo del cuadro de dialogo donde se ingresaran las Envios
      content: Envios.#form,  //content: aqui se almacena el cuerpo de la ventana modal donde se van a ingresar las Envios
      buttons: [                   //recibe un array en que va a ir implementada la funcion de lso botones que van a aparecer dentro de la ventana modal
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Envios.#add() },  //Utilizamos un callBack para no estar cargando siempre una fumcion que solo vamos a utilizar cuando se abra la ventana modal
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Envios.#modal.close() }   //este boton va a tener la funcion  de cerrar la ventana modal
      ],
      doSomething: Envios.#displayDataOnForm  //esta instruccion nos muestra la ventana modal que acabamos de personalizar
    })
    Envios.#modal.show()
  }

  static async #add() {//proximamnete este metodo se encargara de hacer peticiones tipo post
    try {
      if (!Helpers.okForm('#form-envios', Envios.#otherValidations)) {
        return
      }
      const body = Envios.#getFormData()

      let response = await Helpers.fetchJSON(`${urlAPI}/${Envios.#mode}`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Envios.#table.addRow(response.data)
        Envios.#modal.remove()
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
      if (!Helpers.okForm('#form-envios', Envios.#otherValidations)) {
        return
      }
      const body = Envios.#getFormData()
      const url = `${urlAPI}/${Envios.#mode}/${cell.getRow().getData().id}`   //Aqui estamos guardando la direccion o end point especifico de la Envios que deseamos editar

      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })
      if (response.message === 'ok') {
        Toast.show({ message: 'el envio fue actualizada exitosamente!' })
        cell.getRow().update(response.data)
        Envios.#modal.remove()
      }
      else {
        Toast.show({ message: 'No se pudo actualizar la Envios', mode: 'danger', error: response })
      }

    } catch (e) {
      Toast.show({ message: 'Problemas al actualizar Envios (en el try)', mode: 'danger', error: e })
    }
  }

  static async #delete(cell) {//Proximamente este metodo se encargara de hacer la peticion delete 
    try {
      const url = `${urlAPI}/${Envios.#mode}/${cell.getRow().getData().id}`
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
        //Al borrar no necesitamos cargas de ningun tipo, por eso este metodo no contiene body
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'la mercancia ha sido eliminada de manera exitosa!' })
        cell.getRow().delete()
        Envios.#modal.close()
      } else {
        Toast.show({ message: 'No se pudo eliminar el envio', mode: 'danger', error: response })
      }

    } catch (e) {
      Toast.show({ message: 'Problemas al eliminar el envio (try)', mode: 'danger', error: e })
    }
  }

  static #displayDataOnForm(idModal, rowData) {//Este metodo se encarga de mostrar y editar el registro de Envios de la ventana modal
    // referenciar el select "cliente"
    const selectRemitente = document.querySelector(`#${idModal} #remitente`)
    const selectDestinatario = document.querySelector(`#${idModal} #destinatario`)

    if (Envios.#mode === 'sobre' && (Envios.#currentOption === 'edit' || Envios.#currentOption === 'add')) {
      document.querySelector(`#${idModal} #contenido`).value = 'Documentos'
      document.querySelector(`#${idModal} #div-fragil`).style.visibility = 'hidden'
      document.querySelector(`#${idModal} #div-certificado`).style.visibility = 'visible'
      document.querySelector(`#${idModal} #div-peso-valor`).style.display = 'none'
      document.querySelector(`#${idModal} #peso`).value = 0
    } else {
      document.querySelector(`#${idModal} #peso`).min = 0.1
      document.querySelector(`#${idModal} #peso`).step = 0.1
    }

    // asignar la lista de opciones al select "cliente" de Envios.html
    selectRemitente.innerHTML = Envios.#remitente
    selectDestinatario.innerHTML = Envios.#destinatario

    if (Envios.#currentOption === 'edit') {
      //Mostrar todos los datos de la fila actual en el formulario
      document.querySelector(`#${idModal} #id`).value = rowData.id
      document.querySelector(`#${idModal} #contenido`).value = rowData.contenido
      document.querySelector(`#${idModal} #fragil`).value = rowData.fragil
      document.querySelector(`#${idModal} #certificado`).value = rowData.certificado
      document.querySelector(`#${idModal} #peso`).value = rowData.peso
      document.querySelector(`#${idModal} #valorEstimado`).value = rowData.valorEstimado
      selectRemitente.value = rowData.remitente.id
      selectDestinatario.value = rowData.destinatario.id  //como colocarle la informacion al otro cliente
    }
  }

  static #otherValidations(idModal) {
    let respuesta = true
    const selectRemitente = document.querySelector(`#${Envios.#modal.id} #remitente`)
    const selectDestinatario = document.querySelector(`#${Envios.#modal.id} #destinatario`)

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

  static #getFormData() {
    return {
      nroGuia: document.querySelector(`#${Envios.#modal.id} #id`).value,
      remitente: document.querySelector(`#${Envios.#modal.id} #remitente`).value,
      destinatario: document.querySelector(`#${Envios.#modal.id} #destinatario`).value,
      contenido: document.querySelector(`#${Envios.#modal.id} #contenido`).value,
      fragil: document.querySelector(`#${Envios.#modal.id} #fragil`).checked,
      certificado: document.querySelector(`#${Envios.#modal.id} #certificado`).checked,
      peso: parseFloat(document.querySelector(`#${Envios.#modal.id} #peso`).value),
      valorEstimado: parseFloat(document.querySelector(`#${Envios.#modal.id} #valorEstimado`).value)

    }
  }
}
