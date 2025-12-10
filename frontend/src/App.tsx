import { useState } from 'react';
import './App.css';

function App() {
  const [response, setResponse] = useState(null);
  const [inputValue, setInputValue] = useState('');

  const handleSubmit = () => {
    fetch('http://localhost:8080/api/hello', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ message: inputValue }),
    })
    .then(res => res.json())
    .then(data => setResponse(data.message))
    .catch(err => console.log(err));
  };

  return (
    <div className="App">
      <input 
        type="text" 
        name="userInput" 
        value={inputValue} 
        onChange={(e) => setInputValue(e.target.value)} 
      />
      <button id="submitBtn" onClick={handleSubmit}>Submit</button>
      <header className="App-header">
        <p>{response || "Waiting ..."}</p>
      </header>
    </div>
  );
}

export default App;
