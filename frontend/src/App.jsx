import { useEffect, useMemo, useState } from 'react'

const API = 'http://localhost:8080/api'

export default function App() {
  const [menu, setMenu] = useState([])
  const [activeTables, setActiveTables] = useState([])
  const [selectedTable, setSelectedTable] = useState(null)
  const [menuName, setMenuName] = useState('')
  const [menuPrice, setMenuPrice] = useState('')
  const [newTableNumber, setNewTableNumber] = useState('')
  const [selectedMenuItem, setSelectedMenuItem] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [message, setMessage] = useState('')

  const currentTable = useMemo(
    () => activeTables.find((table) => table.id === selectedTable?.id),
    [activeTables, selectedTable]
  )

  async function fetchAll() {
    const [menuRes, tablesRes] = await Promise.all([
      fetch(`${API}/menu`),
      fetch(`${API}/tables/active`)
    ])

    setMenu(await menuRes.json())
    setActiveTables(await tablesRes.json())
  }

  useEffect(() => {
    fetchAll().catch(() => setMessage('Could not connect backend. Start Spring Boot server.'))
  }, [])

  async function addMenuItem(e) {
    e.preventDefault()
    setMessage('')

    const payload = { name: menuName.trim(), price: Number(menuPrice) }
    const res = await fetch(`${API}/menu`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      setMessage('Failed to add menu item. Check name/price.')
      return
    }

    setMenuName('')
    setMenuPrice('')
    await fetchAll()
  }

  async function startTable(e) {
    e.preventDefault()
    setMessage('')

    const tableNumber = Number(newTableNumber)
    if (!tableNumber || tableNumber < 1) {
      setMessage('Table number must be greater than 0.')
      return
    }

    const res = await fetch(`${API}/tables/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tableNumber })
    })

    if (!res.ok) {
      const err = await res.text()
      setMessage(err.includes('already active') ? 'This table is already in progress.' : 'Failed to start table.')
      return
    }

    setNewTableNumber('')
    await fetchAll()
  }

  async function addItemToTable(e) {
    e.preventDefault()
    if (!selectedTable) return
    setMessage('')

    const menuItemId = Number(selectedMenuItem)
    const qty = Number(quantity)

    if (!menuItemId || qty < 1) {
      setMessage('Choose menu item and quantity >= 1.')
      return
    }

    const res = await fetch(`${API}/tables/${selectedTable.id}/items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ menuItemId, quantity: qty })
    })

    if (!res.ok) {
      setMessage('Could not add item to this table.')
      return
    }

    setSelectedMenuItem('')
    setQuantity('1')
    await fetchAll()
  }

  async function serveAndPrintBill() {
    if (!selectedTable) return
    setMessage('')

    const billRes = await fetch(`${API}/tables/${selectedTable.id}/bill`)
    if (!billRes.ok) {
      setMessage('Could not fetch bill.')
      return
    }

    const bill = await billRes.json()

    const printWindow = window.open('', '_blank')
    const rows = bill.items
      .map(
        (item) =>
          `<tr><td>${item.name}</td><td>${item.quantity}</td><td>${Number(item.unitPrice).toFixed(2)}</td><td>${(
            Number(item.unitPrice) * item.quantity
          ).toFixed(2)}</td></tr>`
      )
      .join('')

    printWindow.document.write(`
      <h2>Table ${bill.tableNumber} - Bill</h2>
      <table border="1" cellspacing="0" cellpadding="6">
        <thead><tr><th>Item</th><th>Qty</th><th>Rate</th><th>Total</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
      <h3>Grand Total: ${Number(bill.total).toFixed(2)}</h3>
    `)
    printWindow.document.close()
    printWindow.focus()
    printWindow.print()

    const serveRes = await fetch(`${API}/tables/${selectedTable.id}/serve`, { method: 'POST' })
    if (serveRes.ok) {
      setSelectedTable(null)
      await fetchAll()
    }
  }

  return (
    <div className="layout">
      <main className="content">
        <h1>Hotel Table Management</h1>
        {message && <p className="message">{message}</p>}

        <section className="card">
          <h2>Add Menu Item</h2>
          <form onSubmit={addMenuItem} className="row-form">
            <input placeholder="Item name" value={menuName} onChange={(e) => setMenuName(e.target.value)} required />
            <input
              placeholder="Price"
              type="number"
              min="0.01"
              step="0.01"
              value={menuPrice}
              onChange={(e) => setMenuPrice(e.target.value)}
              required
            />
            <button type="submit">Add Menu</button>
          </form>
        </section>

        <section className="card">
          <h2>Start Table</h2>
          <form onSubmit={startTable} className="row-form">
            <input
              placeholder="Table number"
              type="number"
              min="1"
              value={newTableNumber}
              onChange={(e) => setNewTableNumber(e.target.value)}
              required
            />
            <button type="submit">Start Table</button>
          </form>
        </section>

        <section className="card">
          <h2>Menu</h2>
          <ul className="menu-list">
            {menu.map((item) => (
              <li key={item.id}>
                {item.name} - ₹{Number(item.price).toFixed(2)}
              </li>
            ))}
          </ul>
        </section>
      </main>

      <aside className="sidebar">
        <h3>Ongoing Tables</h3>
        {activeTables.length === 0 && <p>No active tables</p>}
        {activeTables.map((table) => (
          <button key={table.id} className="table-pill" onClick={() => setSelectedTable(table)}>
            Table {table.tableNumber}
          </button>
        ))}
      </aside>

      {selectedTable && (
        <div className="modal-backdrop" onClick={() => setSelectedTable(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Table {selectedTable.tableNumber}</h3>
            <form onSubmit={addItemToTable} className="col-form">
              <select value={selectedMenuItem} onChange={(e) => setSelectedMenuItem(e.target.value)} required>
                <option value="">Select Menu Item</option>
                {menu.map((item) => (
                  <option value={item.id} key={item.id}>
                    {item.name} (₹{Number(item.price).toFixed(2)})
                  </option>
                ))}
              </select>
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                required
              />
              <button type="submit">Add Item</button>
            </form>

            <h4>Current Items</h4>
            <ul className="menu-list">
              {(currentTable?.items ?? []).map((item) => (
                <li key={item.menuItemId}>
                  {item.name} x {item.quantity} = ₹{(Number(item.unitPrice) * item.quantity).toFixed(2)}
                </li>
              ))}
            </ul>
            <p className="total">Total: ₹{Number(currentTable?.total || 0).toFixed(2)}</p>

            <div className="actions">
              <button onClick={serveAndPrintBill}>Serve & Print Bill</button>
              <button className="secondary" onClick={() => setSelectedTable(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
