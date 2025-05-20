export default class Clientes {
  static #table
  static #modal
  static #currentOption
  static #form
  static #ciudades

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Clientes.init()')
  }

  static async init() {
    try {
      Clientes.#form = await Helpers.fetchText('./resources/html/clientes.html')

      Clientes.#ciudades = Helpers.toOptionList({
        items: await Helpers.fetchJSON('./resources/assets/ciudades.json'),
        value: 'codigo',
        text: 'nombre',
        firstOption: 'Seleccione ciudad',
      })

      let response = await Helpers.fetchJSON(`${urlAPI}/cliente`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
      <div class="p-2 w-full">
          <div id="table-container" class="m-2"></div>
      </dv>`

      Clientes.#table = new Tabulator('#table-container', {
        height: tableHeight, // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitDataFill', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Clientes.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Clientes.#deleteRowClick },
          { title: 'Id', field: 'id', hozAlign: 'center', width: 90 },
          { title: 'Nombre', field: 'nombre', width: 230 },
          { title: 'Dirección', field: 'direccion', width: 400 },
          { title: 'Teléfono', field: 'telefono', width: 150 },
          { title: 'Ciudad', field: 'ciudad', width: 220 },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        initialSort: [
          // establecer el ordenamiento inicial de los datos
          { column: 'nombre', dir: 'asc' },
        ],
        columnDefaults: {
          tooltip: true, //show tool tips on cells
        },

        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      Clientes.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Clientes.#addRow))
    } catch (e) {
      Toast.show({ title: 'Clientes', message: 'Falló la carga de la información', mode: 'danger', error: e })
    }
    return this
  }

  static async #addRow() {
    Clientes.#currentOption = 'add'
    Clientes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class="text-secondary">Ingreso de clientes</h5>`,
      content: Clientes.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Clientes.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clientes.#modal.close() },
      ],
      doSomething: Clientes.#displayDataOnForm,
    })
    Clientes.#modal.show()
  }

  static #editRowClick = async (e, cell) => {
    Clientes.#currentOption = 'edit'
    console.log(cell.getRow().getData())

    Clientes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class="text-secondary">Actualización de clientes</h5>`,
      content: Clientes.#form,
      buttons: [
        { caption: editButton, classes: 'btn btn-primary me-2', action: () => Clientes.#edit(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clientes.#modal.close() },
      ],
      doSomething: idModal => Clientes.#displayDataOnForm(idModal, cell.getRow().getData()),
    })
    Clientes.#modal.show()
  }

  static #deleteRowClick = async (e, cell) => {
    Clientes.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    Clientes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class="text-secondary">Eliminación de clientes</h5>`,
      content: `<span class="text-back dark:text-gray-300">
                  Confirme la eliminación del cliente:<br>
                  ${cell.getRow().getData().id} - ${cell.getRow().getData().nombre}<br>
                  Ciudad: ${cell.getRow().getData().ciudad}<br>
                  Teléfono: ${cell.getRow().getData().telefono}<br>
                </span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Clientes.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clientes.#modal.close() },
      ],
    })
    Clientes.#modal.show()
  }

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-clientes')) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud POST
      const body = Clientes.#getFormData()

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(`${urlAPI}/cliente`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Clientes.#table.addRow(response.data) // agregar la mercancía a la tabla
        Clientes.#modal.remove()
        Toast.show({ message: 'Agregado exitosamente' })
      } else {
        Toast.show({ message: 'No se pudo agregar el registro', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Falló la operación de creación del registro', mode: 'danger', error: e })
    }
  }

  static async #edit(cell) {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-clientes')) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Clientes.#getFormData()

      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/cliente/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Información de cliente actualizado exitosamente' })
        cell.getRow().update(response.data)
        Clientes.#modal.remove()
      } else {
        Toast.show({ message: 'No se pudo actualizar el cliente', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problemas al actualizar el cliente', mode: 'danger', error: e })
    }
  }

  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud DELETE
      const url = `${urlAPI}/cliente/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Cliente eliminado exitosamente' })
        cell.getRow().delete()
        Clientes.#modal.remove()
      } else {
        Toast.show({ message: 'No se pudo eliminar el cliente', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problemas al eliminar el cliente', mode: 'danger', error: e })
    }
  }

  static #displayDataOnForm(idModal, rowData) {
    // referenciar el select "cliente"
    const selectCiudad = document.querySelector(`#${idModal} #ciudad`)
    // asignar la lista de opciones al select "cliente" de Clientes.html
    selectCiudad.innerHTML = Clientes.#ciudades

    if (Clientes.#currentOption === 'edit') {
      // mostrar los datos de la fila actual en el formulario html
      document.querySelector(`#${idModal} #id`).value = rowData.id
      document.querySelector(`#${idModal} #nombre`).value = rowData.nombre
      document.querySelector(`#${idModal} #direccion`).value = rowData.direccion
      document.querySelector(`#${idModal} #telefono`).value = rowData.telefono
      Helpers.selectOptionByText(selectCiudad, rowData.ciudad)
    }
  }

  /**
   * Recupera los datos del formulario y crea un objeto para ser retornado
   * @returns Un objeto con los datos del usuario
   */
  static #getFormData() {
    const ciudad = document.querySelector(`#${Clientes.#modal.id} #ciudad`)
    const index = ciudad.selectedIndex
    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      id: document.querySelector(`#${Clientes.#modal.id} #id`).value,
      nombre: document.querySelector(`#${Clientes.#modal.id} #nombre`).value,
      direccion: document.querySelector(`#${Clientes.#modal.id} #direccion`).value,
      telefono: document.querySelector(`#${Clientes.#modal.id} #telefono`).value,
      ciudad: ciudad.options[index].text,
    }
  }
}
