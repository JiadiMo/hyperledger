import logo from './logo.svg';
import './App.css';
import JSEncrypt from 'jsencrypt';
import axios from 'axios';
// import uuid from 'uuid';
import { useState } from 'react';

const privateK = 'MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDoqC1BKtD7tl0Nx+Cqtc4VQ2NFN35a5qhZftF6DCZB0u4qgzlDr8leY9QndTh5TcWCkdtWsONWbNzsaXYhwMYhS4l+1z3JGhbU1PuXfs+ZUoq/W2ybqkTBjlmUbh8TUMYsrZhG+ZvecS//XnSOOqlJ+5dBeQNlBcwfYWJWTkjIwYSmK76Beo4LXYK0RTJOb2Ipi6RBdHSKKcF44RuOsyyX2TcsqHJrJLlQHI+mQTPEPaHQX8yInKPiKtY2tWGN+JmjxaBYEfVW3o1QtbkOGLWOt5bIxwB1HMjbKNi70bBmO+X4Qzel2T6y5s/dheZ5fr7ZMZroMJiP/nQXZMydENTNAgMBAAECggEADjxMGJKwGta7tt4mBwv8whxPdKrClnlKj79MvfUEWj63x+4jkNqurZHaJ1fqzrFNhoG6NAG6x0z975E8YzBSbzMaBKLS6v0nrHojss9VkIIWb0L+0D+I2XcGUCi0yg/FMHKmwKEBQokWcL+szg8U0KwOQzlOVd4W8UyaIcHEflQLE8O9SXZpJ/9F0D30ZJiY1QZsbYE88kitcZ0O5n49tXJ8g9T9k6SyCQuqpf+xvkyRdMnv8GA7vnK7IrxoF/yqqSGcCCZNaVCHxDIFSCw02MZ04dLFK4Qb6IW0X7VSgOaQTtHPkx5sPorXEBnVX+TFUs6ai6eXeJ/c3g5sNxcCgQKBgQD6Pl5Hl0wtTqogvNb9xldtsB38bNAJKD672gJ1d9JQUpjimPLqjG0TZEUfWTkNIR16wLg4BQTopmi5fF28LfgOoD5yZCNh7RMJ9XGXD8m9LpbdrZ3DlYh5JuYc3x/uWRo8WTLpR76z56NxxcIP1egEzhfJaG68XCijSBpfPmx8HQKBgQDuAj6FxtU3mQzmJapuFJtgAXS2hLxEV7pD6QUs8u1cOgC/bOz7YXH0diOOU8bsKOpJcsYMpESagO5Q+76A9fqALH8PtTgl4zkfNVvJf43aDW9ldRIQbBMB5/8BfFCUU8xsHlqXV3blYsBDuwLyPEklk9oE8BuMQrBPWLh4glB8cQKBgQDMHTMpke9AxXlfWqjCNpX5kj3jN5442erRrvFyf1m8yqKJsdfyGLifqJn5B/3RB8HT7n5Us0NdwN6K7TZH1/cNdwd4ptV1erqc1ObiIK6c8PN9va+BaOb4axInWpfhAiy0LM2qGisi8z4N/xBbek7WHISqc9RfL4y2IongRjqWwQKBgGowpiSZqde8eXzVoUyr9QbIn4IvhrAXVAuok56SZESZvGur9a/Ssj0X2JVP25jSSHWst6A8Sj+E9s87AqwXE0TMlYQ0nnCJCJtb+y/TsT5wcBscOIsLLQ/UqnnHNUx4duRmRpTveSSvAok1jwL00s3pQYSifCgAORmtw+BAhBiBAoGAMtihoBQ4J/QORSA5aFjoKm0itgMOEazXfG6D3YjqBezNWfQmy2NO3A8pi6GACyeiYy4mdHLKeTeCEm5IRdt2x+JhhP3i7KAdATK4ezXLqHStA4z09+Ms817tQNUHkaRosAWwFP8H9ONX1fimCGbiOOESFghLpoeLTkWXHQaT2bU='
const publicK = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6KgtQSrQ+7ZdDcfgqrXOFUNjRTd+WuaoWX7RegwmQdLuKoM5Q6/JXmPUJ3U4eU3FgpHbVrDjVmzc7Gl2IcDGIUuJftc9yRoW1NT7l37PmVKKv1tsm6pEwY5ZlG4fE1DGLK2YRvmb3nEv/150jjqpSfuXQXkDZQXMH2FiVk5IyMGEpiu+gXqOC12CtEUyTm9iKYukQXR0iinBeOEbjrMsl9k3LKhyayS5UByPpkEzxD2h0F/MiJyj4irWNrVhjfiZo8WgWBH1Vt6NULW5Dhi1jreWyMcAdRzI2yjYu9GwZjvl+EM3pdk+subP3YXmeX6+2TGa6DCYj/50F2TMnRDUzQIDAQAB';

const encrypt = content => {
  const encryptor = new JSEncrypt()
  
  encryptor.setPrivateKey(privateK)//设置公钥
  const encryptedContent = encryptor.encrypt(content)  // 对内容进行加密
  console.log('encryptedContent: ', encryptedContent);
  encryptor.setPublicKey(publicK)
  const rere = encryptor.decrypt(encryptedContent);
  console.log('de', rere);
  return encryptedContent;
}

const decrypt = secret => {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(publicK)
  return encryptor.decrypt(secret);
}

console.log(decrypt('yU/9LakzTctmd7PY8YBS54Y8VQImoRjBAq0C6qEXIMcG4beFRPy/aaf7pGsrTN+h37RRJTu3aW71RRrNdYWRjxWJEC5etbiKRg5Zy3qj9hoDPXmoPwlNftyN8CG98QR1ZXe0tz4La4cSJe1f3RQFrE8T5UG5k2GMydgs0xAR6OICmBbwivNhM8EIQ+h/fRc92hcPUgZFYHkZAVWc26gMuKyI+cN0XeBvArqGvoDJdaHRVgKaIUw45dhSMhmxP/8rlLaFBcKDze1KXBtxzhd7umnNfQWkUWRvpIasRmvMOfJDpCrfKFYC3q40Lv1lAa2/YlyAHzfOpSshP2+vPRwr5w=='))

function App() {
  const [val, setVal] = useState(0);
  const requestBody = {
    id: Date.now(),
    timestamp: Date.now(),
    params: {
      from: '123',
      to: '456',
      amount: 10.5,
    },
  };
  const callApi = (from, to, amount) => {
    axios.post(`http://localhost:8090/send`, {
      ...requestBody,
      signature: encrypt(JSON.stringify(requestBody)),
    })
  };
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          {val}
        </p>
        <input onChange={v => setVal(Number(v.target.value))} />
        <button onClick={() => callApi(2, 43, 10)}>OK</button>
      </header>
    </div>
  );
}

export default App;
