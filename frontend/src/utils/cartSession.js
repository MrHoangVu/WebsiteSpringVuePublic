// src/utils/cartSession.js
import { v4 as uuidv4 } from 'uuid';

const CART_SESSION_KEY = 'cart_session_id';

export const getCartSessionId = () => {
  let sessionId = localStorage.getItem(CART_SESSION_KEY);
  if (!sessionId) {
    sessionId = uuidv4();
    localStorage.setItem(CART_SESSION_KEY, sessionId);
    console.log('Generated new Cart Session ID:', sessionId);
  }
  return sessionId;
};

export const clearCartSessionId = () => {
  localStorage.removeItem(CART_SESSION_KEY);
  console.log('Cleared Cart Session ID from localStorage.');
};
