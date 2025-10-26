import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios'; 
import './App.css';

// API base URL
const API_URL = 'http://localhost:8080';

// Global stompClient variable
let stompClient = null;

// --- Auth Component (No Changes) ---
function AuthPage({ onLoginSuccess }) {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(''); 

    const endpoint = isLogin ? '/api/auth/login' : '/api/auth/register';
    const payload = { username, password };

    try {
      if (isLogin) {
        const response = await axios.post(`${API_URL}${endpoint}`, payload);
        const { token } = response.data;
        if (token) {
          localStorage.setItem('authToken', token);
          localStorage.setItem('username', username);
          onLoginSuccess(username, token); 
        }
      } else {
        await axios.post(`${API_URL}${endpoint}`, payload);
        setIsLogin(true);
        setError('Registration successful! Please log in.');
      }
    } catch (err) {
      console.error('Authentication error:', err);
      if (err.response && err.response.data) {
        if (typeof err.response.data === 'object') {
          setError(err.response.data.error || 'Server error occurred.'); 
        } else {
          setError(err.response.data.message || err.response.data);
        }
      } 
      else {
        setError('An error occurred. Please try again.');
      }
    }
  };

  return (
    <div className="auth-container">
      <h2>{isLogin ? 'Login' : 'Register'}</h2>
      <form className="auth-form" onSubmit={handleSubmit}>
        {error && <div className="error-message">{error}</div>}
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">{isLogin ? 'Login' : 'Register'}</button>
      </form>
      <button className="auth-toggle" onClick={() => setIsLogin(!isLogin)}>
        {isLogin
          ? "Don't have an account? Register"
          : 'Already have an account? Login'}
      </button>
    </div>
  );
}

// --- Chat App Component (Updated) ---
function ChatApp({ currentUser, token, onLogout }) {
  // --- STATE UPDATED ---
  // messages is now a map: { 'chatName': [message1, message2], ... }
  const [messages, setMessages] = useState({}); 
  const [message, setMessage] = useState('');
  const messageAreaRef = useRef(null);
  
  const [users, setUsers] = useState([]); 
  const [activeChat, setActiveChat] = useState('public-chat');
  // --- END UPDATE ---

  // Scroll to bottom when new messages arrive
  useEffect(() => {
    if (messageAreaRef.current) {
      messageAreaRef.current.scrollTop = messageAreaRef.current.scrollHeight;
    }
  }, [messages, activeChat]); // Also trigger on activeChat change

  // Connect to WebSocket and Fetch Users on component mount
  useEffect(() => {
    connectToWebSocket();
    fetchUsers(); 

    // Disconnect on component unmount
    return () => {
      if (stompClient) {
        stompClient.deactivate();
        console.log('WebSocket disconnected.');
      }
    };
  }, []); 

  // --- NEW: Helper function to add a message to the state ---
  const addMessageToChat = (chatName, message) => {
    setMessages(prevMessages => {
      // Get the existing chat array, or create a new one
      const chatMessages = prevMessages[chatName] ? [...prevMessages[chatName]] : [];
      chatMessages.push(message);
      
      // Return the new state
      return {
        ...prevMessages,
        [chatName]: chatMessages
      };
    });
  };
  
  const fetchUsers = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/users`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      const userList = ['public-chat', ...response.data.map(user => user.username)];
      setUsers(userList);

      // --- NEW: Initialize the message map ---
      const initialMessages = {};
      userList.forEach(user => {
        initialMessages[user] = []; // Initialize empty array for each chat
      });
      setMessages(initialMessages);
      // --- END NEW ---

    } catch (error) {
      console.error('Failed to fetch users:', error);
      setUsers(['public-chat']); 
      setMessages({'public-chat': []});
    }
  };

  const connectToWebSocket = () => {
    console.log('Attempting to connect with token...');
    
    const socket = new SockJS(`${API_URL}/ws`);
    stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => { console.log(new Date(), str); },
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: onConnected,
      onStompError: onError,
      onError: onError,
    });

    stompClient.activate();
  };

  const onConnected = () => {
    console.log('Connected to WebSocket!');
    
    // --- UPDATED: Subscribe to public AND private topics ---
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onPublicMessageReceived);
    
    // Subscribe to your own private queue
    stompClient.subscribe(`/user/${currentUser}/queue/private`, onPrivateMessageReceived);
    // --- END UPDATE ---

    // Send the JOIN message to the public topic
    stompClient.publish({
      destination: '/app/chat.addUser',
      body: JSON.stringify({ sender: currentUser, type: 'JOIN' }),
    });

    // Add a system message to the public chat
    addMessageToChat('public-chat', {
      sender: 'System',
      content: 'You are connected.',
      type: 'EVENT'
    });
  };

  const onError = (error) => {
    console.error('WebSocket Error:', error);
    let errorMessage = 'Error connecting to chat. Please refresh.';
    if (error.headers && error.headers.message) {
      errorMessage = error.headers.message;
    }
    // Add error to public chat
    addMessageToChat('public-chat', {
      sender: 'System',
      content: errorMessage,
      type: 'EVENT'
    });
  };

  // --- UPDATED: Handler for PUBLIC messages ---
  const onPublicMessageReceived = (payload) => {
    const msg = JSON.parse(payload.body);
    // Add all public messages to the 'public-chat'
    addMessageToChat('public-chat', msg);
  };
  
  // --- NEW: Handler for PRIVATE messages ---
  const onPrivateMessageReceived = (payload) => {
    const msg = JSON.parse(payload.body);
    // A private message's "chatName" is the sender's username
    addMessageToChat(msg.sender, msg);
  };
  
  // --- UPDATED: Send message logic ---
  const sendMessage = (event) => {
    event.preventDefault();
    if (message.trim() && stompClient) {
      const isPublic = activeChat === 'public-chat';
      
      const chatMessage = {
        sender: currentUser,
        content: message,
        type: 'CHAT',
        recipient: isPublic ? null : activeChat // Set recipient if it's a private chat
      };

      // Send to the correct backend destination
      const destination = isPublic 
        ? '/app/chat.sendMessage'       // Public
        : '/app/chat.sendPrivateMessage'; // Private

      stompClient.publish({
        destination: destination,
        body: JSON.stringify(chatMessage),
      });

      // --- NEW: Add your own sent message to the correct chat window ---
      if (!isPublic) {
        addMessageToChat(activeChat, chatMessage);
      }
      // --- END NEW ---

      setMessage('');
    }
  };

  // --- RENDER LOGIC UPDATED ---
  const currentChatMessages = messages[activeChat] || []; // Get messages for active chat

  return (
    <div className="chat-app-container">
      {/* --- User List Sidebar --- */}
      <div className="user-list-container">
        <div className="user-list-header">
          <span>Hello, {currentUser}</span>
          <button onClick={onLogout}>Logout</button>
        </div>
        <ul className="user-list">
          {users.map((user) => (
            <li
              key={user}
              className={`user-list-item ${user === activeChat ? 'active' : ''}`}
              onClick={() => setActiveChat(user)}
            >
              {user}
            </li>
          ))}
        </ul>
      </div>

      {/* --- Chat Window (Updated) --- */}
      <div className="chat-window">
        <div className="chat-header">
          <h2>Chat with {activeChat}</h2>
        </div>

        {/* Display messages for the *active* chat */}
        <ul className="message-area" ref={messageAreaRef}>
          {currentChatMessages.map((msg, index) => (
            <li
              key={index}
              className={`message ${
                msg.type === 'EVENT'
                  ? 'event'
                  : msg.sender === currentUser
                  ? 'sent'
                  : 'received'
              }`}
            >
              {msg.type === 'CHAT' && msg.sender !== currentUser && (
                <span className="message-sender">{msg.sender}</span>
              )}
              <span className="message-content">
                {msg.type === 'EVENT' ? `${msg.sender} ${msg.type.toLowerCase()}!` : msg.content}
              </span>
            </li>
          ))}
        </ul>

        <form
          className="input-area"
          onSubmit={sendMessage}
          autoComplete="off"
        >
          <input
            type="text"
            placeholder="Type a message..."
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            // --- UPDATED: Now enabled for all chats ---
            disabled={!stompClient} 
          />
          <button type="submit" disabled={!stompClient}>
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
// --- END CHAT APP ---


// --- Main App Component (No Changes) ---
function App() {
  const [auth, setAuth] = useState(null); 

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const username = localStorage.getItem('username');
    if (token && username) {
      setAuth({ username, token });
    }
  }, []);

  const handleLoginSuccess = (username, token) => {
    setAuth({ username, token });
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    setAuth(null);
    
    if (stompClient) {
      stompClient.deactivate();
      console.log('WebSocket disconnected due to logout.');
    }
  };

  if (!auth) {
    return <AuthPage onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <ChatApp
      currentUser={auth.username}
      token={auth.token}
      onLogout={handleLogout}
    />
  );
}

export default App;

