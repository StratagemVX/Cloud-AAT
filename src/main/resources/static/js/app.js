/**
 * ═══════════════════════════════════════════════════════════════════════
 *  ChatApp — Client-side WebSocket/STOMP Application Logic
 *
 *  Handles:
 *  1. Login flow (username selection)
 *  2. WebSocket connection via SockJS + STOMP
 *  3. Sending/receiving real-time messages
 *  4. @bot prefix detection for chatbot interaction
 *  5. Online users list management
 *  6. Message rendering with proper styling
 * ═══════════════════════════════════════════════════════════════════════
 */

// ─── DOM Element References ────────────────────────────────────────────
const loginScreen = document.getElementById('login-screen');
const chatScreen = document.getElementById('chat-screen');
const loginForm = document.getElementById('login-form');
const usernameInput = document.getElementById('username-input');
const messageForm = document.getElementById('message-form');
const messageInput = document.getElementById('message-input');
const messageArea = document.getElementById('message-area');
const onlineUsersList = document.getElementById('online-users-list');
const onlineCount = document.getElementById('online-count');
const currentUserDisplay = document.getElementById('current-user-display');
const connectionStatus = document.getElementById('connection-status');
const logoutBtn = document.getElementById('logout-btn');
const clearHistoryBtn = document.getElementById('clear-history-btn');
const randomQuestionBtn = document.getElementById('random-question-btn');
const faqBtn = document.getElementById('faq-btn');
const faqPanel = document.getElementById('faq-panel');
const faqOverlay = document.getElementById('faq-overlay');
const faqCloseBtn = document.getElementById('faq-close-btn');
const faqList = document.getElementById('faq-list');

// Switch-user elements
const switchUserBtn = document.getElementById('switch-user-btn');
const switchUserOverlay = document.getElementById('switch-user-overlay');
const switchUserModal = document.getElementById('switch-user-modal');
const switchModalClose = document.getElementById('switch-modal-close');
const switchUserList = document.getElementById('switch-user-list');
const switchNewUsername = document.getElementById('switch-new-username');
const switchNewUserBtn = document.getElementById('switch-new-user-btn');
const totalUserCount = document.getElementById('total-user-count');

// ─── State Variables ───────────────────────────────────────────────────
let stompClient = null;   // STOMP client instance
let currentUser = null;   // Currently logged-in username
let isConnected = false;  // WebSocket connection state
let refreshInterval = null; // Interval ID for periodic refresh

// Color palette for user avatars (deterministic per username)
const avatarColors = [
    '#7C3AED', '#2563EB', '#10B981', '#F59E0B', '#EF4444',
    '#EC4899', '#8B5CF6', '#06B6D4', '#84CC16', '#F97316'
];

// ═══════════════════════════════════════════════════════════════════════
//  LOGIN FLOW
// ═══════════════════════════════════════════════════════════════════════

/**
 * Handle login form submission.
 * Validates the username and initiates the WebSocket connection.
 */
loginForm.addEventListener('submit', function (e) {
    e.preventDefault();
    const username = usernameInput.value.trim();

    if (username.length < 2) {
        shakeElement(usernameInput);
        return;
    }

    currentUser = username;
    connectWebSocket();
});

// ═══════════════════════════════════════════════════════════════════════
//  WEBSOCKET CONNECTION
// ═══════════════════════════════════════════════════════════════════════

/**
 * Establish a WebSocket connection using SockJS and STOMP.
 *
 * Flow:
 * 1. Create a SockJS socket pointing to the /ws endpoint
 * 2. Wrap it with a STOMP client
 * 3. On successful connection, subscribe to /topic/public
 * 4. Send a JOIN message to announce the user
 */
function connectWebSocket() {
    // Create SockJS connection (with fallback transports)
    const socket = new SockJS('/ws');

    // Create STOMP client over the SockJS connection
    stompClient = Stomp.over(socket);

    // Disable debug logging in production
    stompClient.debug = null;

    // Connect to the STOMP broker
    stompClient.connect({}, onConnected, onConnectionError);
}

/**
 * Callback when STOMP connection succeeds.
 * Subscribes to the public channel and announces the user.
 */
function onConnected() {
    isConnected = true;

    // Switch from login screen to chat screen
    loginScreen.classList.add('hidden');
    chatScreen.classList.remove('hidden');

    // Display current user in the header
    currentUserDisplay.textContent = currentUser;

    // Update connection status indicator
    updateConnectionStatus(true);

    // Subscribe to the public broadcast topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Announce that this user has joined
    stompClient.send('/app/chat.addUser', {}, JSON.stringify({
        sender: currentUser,
        type: 'JOIN'
    }));

    // Load chat history and online users
    loadMessageHistory();
    refreshOnlineUsers();
    fetchUserCount();

    // Periodically refresh online users list and user count (every 10 seconds)
    if (refreshInterval) clearInterval(refreshInterval);
    refreshInterval = setInterval(() => {
        refreshOnlineUsers();
        fetchUserCount();
    }, 10000);

    // Focus the message input
    messageInput.focus();
}

/**
 * Callback when the WebSocket connection fails or is lost.
 */
function onConnectionError(error) {
    isConnected = false;
    updateConnectionStatus(false);
    console.error('WebSocket connection error:', error);
}

// ═══════════════════════════════════════════════════════════════════════
//  SENDING MESSAGES
// ═══════════════════════════════════════════════════════════════════════

/**
 * Handle message form submission.
 * Sends the message via STOMP to the server.
 */
messageForm.addEventListener('submit', function (e) {
    e.preventDefault();
    const content = messageInput.value.trim();

    if (content && isConnected) {
        const message = {
            sender: currentUser,
            content: content,
            type: 'CHAT'
        };

        // Send the message to the server via STOMP
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));

        // Clear the input field
        messageInput.value = '';
        messageInput.focus();
    }
});

// ═══════════════════════════════════════════════════════════════════════
//  RECEIVING & RENDERING MESSAGES
// ═══════════════════════════════════════════════════════════════════════

/**
 * Callback triggered when a message is received on /topic/public.
 * Parses the message and delegates to the appropriate renderer.
 */
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);

    switch (message.type) {
        case 'JOIN':
            renderSystemMessage(message.content || message.sender + ' joined!', 'join');
            refreshOnlineUsers();
            break;

        case 'LEAVE':
            renderSystemMessage(message.content || message.sender + ' left!', 'leave');
            refreshOnlineUsers();
            break;

        case 'CHAT':
            renderChatMessage(message);
            break;

        case 'BOT':
            renderBotMessage(message);
            break;
    }
}

/**
 * Render a regular chat message bubble.
 * Determines whether the message is "sent" (by current user) or "received".
 */
function renderChatMessage(message) {
    const isSent = message.sender === currentUser;

    const div = document.createElement('div');
    div.className = `message ${isSent ? 'sent' : 'received'}`;

    const avatarColor = getAvatarColor(message.sender);
    const initials = message.sender.substring(0, 2).toUpperCase();

    div.innerHTML = `
        <div class="message-avatar" style="background: ${avatarColor}">
            ${initials}
        </div>
        <div class="message-bubble">
            <span class="message-sender">${escapeHtml(message.sender)}</span>
            <span class="message-text">${escapeHtml(message.content)}</span>
            <span class="message-time">${message.timestamp || ''}</span>
        </div>
    `;

    // Remove the welcome message on first real message
    removeWelcome();
    messageArea.appendChild(div);
    scrollToBottom();
}

/**
 * Render a chatbot response with special bot styling.
 */
function renderBotMessage(message) {
    const div = document.createElement('div');
    div.className = 'message bot received';

    div.innerHTML = `
        <div class="message-avatar">🤖</div>
        <div class="message-bubble">
            <span class="message-sender">ChatBot</span>
            <span class="message-text">${escapeHtml(message.content)}</span>
            <span class="message-time">${message.timestamp || ''}</span>
        </div>
    `;

    removeWelcome();
    messageArea.appendChild(div);
    scrollToBottom();
}

/**
 * Render a system event message (join/leave).
 */
function renderSystemMessage(text, type) {
    const div = document.createElement('div');
    div.className = `system-message ${type}`;
    div.innerHTML = `<span>${escapeHtml(text)}</span>`;

    removeWelcome();
    messageArea.appendChild(div);
    scrollToBottom();
}

// ═══════════════════════════════════════════════════════════════════════
//  ONLINE USERS MANAGEMENT
// ═══════════════════════════════════════════════════════════════════════

/**
 * Fetch the list of online users from the REST API
 * and update the sidebar.
 */
function refreshOnlineUsers() {
    fetch('/api/users/online')
        .then(response => response.json())
        .then(users => {
            onlineUsersList.innerHTML = '';
            onlineCount.textContent = users.length;

            users.forEach(user => {
                const li = document.createElement('li');
                const color = getAvatarColor(user.username);
                const initials = user.username.substring(0, 2).toUpperCase();
                const isCurrentUser = user.username === currentUser;

                li.innerHTML = `
                    <div class="user-avatar" style="background: ${color}">
                        ${initials}
                    </div>
                    <span class="user-name">
                        ${escapeHtml(user.username)}${isCurrentUser ? ' (You)' : ''}
                    </span>
                    <span class="online-indicator"></span>
                `;

                onlineUsersList.appendChild(li);
            });
        })
        .catch(err => console.error('Failed to fetch online users:', err));
}

// ═══════════════════════════════════════════════════════════════════════
//  MESSAGE HISTORY
// ═══════════════════════════════════════════════════════════════════════

/**
 * Load recent message history from the REST API.
 * Called once when the user connects.
 */
function loadMessageHistory() {
    fetch('/api/messages')
        .then(response => response.json())
        .then(messages => {
            if (messages.length > 0) {
                removeWelcome();
            }
            messages.forEach(msg => {
                switch (msg.type) {
                    case 'JOIN':
                        renderSystemMessage(msg.content, 'join');
                        break;
                    case 'LEAVE':
                        renderSystemMessage(msg.content, 'leave');
                        break;
                    case 'BOT':
                        renderBotMessage(msg);
                        break;
                    default:
                        renderChatMessage(msg);
                }
            });
        })
        .catch(err => console.error('Failed to load message history:', err));
}

// ═══════════════════════════════════════════════════════════════════════
//  LOGOUT
// ═══════════════════════════════════════════════════════════════════════

/**
 * Handle logout: disconnect from WebSocket and return to login screen.
 */
logoutBtn.addEventListener('click', function () {
    if (stompClient) {
        stompClient.disconnect();
    }

    // Notify the server
    fetch(`/api/users/disconnect?username=${encodeURIComponent(currentUser)}`, {
        method: 'POST'
    }).catch(() => { });

    // Reset state
    isConnected = false;
    currentUser = null;
    stompClient = null;
    if (refreshInterval) { clearInterval(refreshInterval); refreshInterval = null; }

    // Switch screens
    chatScreen.classList.add('hidden');
    loginScreen.classList.remove('hidden');

    // Clear messages
    messageArea.innerHTML = `
        <div class="welcome-message">
            <div class="welcome-icon">💬</div>
            <h3>Welcome to the chat!</h3>
            <p>Messages will appear here. The AI assistant replies to every message automatically!</p>
        </div>
    `;

    usernameInput.value = '';
    usernameInput.focus();
});

// ═══════════════════════════════════════════════════════════════════════
//  UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════

/**
 * Get a deterministic color for a username (consistent avatar color).
 */
function getAvatarColor(username) {
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    return avatarColors[Math.abs(hash) % avatarColors.length];
}

/**
 * Escape HTML entities to prevent XSS.
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

/**
 * Scroll the message area to the bottom (latest message).
 */
function scrollToBottom() {
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * Remove the static welcome message (shown before any messages arrive).
 */
function removeWelcome() {
    const welcome = messageArea.querySelector('.welcome-message');
    if (welcome) {
        welcome.remove();
    }
}

/**
 * Apply a shake animation to an element (for input validation).
 */
function shakeElement(el) {
    el.style.animation = 'none';
    el.offsetHeight; // Force reflow
    el.style.animation = 'shake 0.4s ease';
    setTimeout(() => el.style.animation = '', 400);
}

/**
 * Update the connection status badge in the header.
 */
function updateConnectionStatus(connected) {
    const dot = connectionStatus.querySelector('.status-dot');
    if (connected) {
        connectionStatus.innerHTML = '<span class="status-dot"></span> Connected';
        connectionStatus.style.color = '#34D399';
    } else {
        connectionStatus.innerHTML = '<span class="status-dot" style="background:#EF4444"></span> Disconnected';
        connectionStatus.style.color = '#EF4444';
    }
}

// ─── CSS Shake Animation (injected dynamically) ───────────────────────
const shakeStyle = document.createElement('style');
shakeStyle.textContent = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-6px); }
        50% { transform: translateX(6px); }
        75% { transform: translateX(-4px); }
    }
`;
document.head.appendChild(shakeStyle);

// ═══════════════════════════════════════════════════════════════════
//  CLEAR CHAT HISTORY
// ═══════════════════════════════════════════════════════════════════

/**
 * Handle clear history button click.
 * Deletes all messages from the server and clears the chat area.
 */
clearHistoryBtn.addEventListener('click', function () {
    fetch('/api/messages', { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                // Clear the message area in the UI
                messageArea.innerHTML = `
                    <div class="welcome-message">
                        <div class="welcome-icon">🗑️</div>
                        <h3>Chat history cleared!</h3>
                        <p>Start a new conversation. The AI assistant replies to every message automatically!</p>
                    </div>
                `;
            } else {
                console.error('Failed to clear history:', response.status);
            }
        })
        .catch(err => console.error('Failed to clear history:', err));
});

// ═══════════════════════════════════════════════════════════════════
//  RANDOM QUESTION
// ═══════════════════════════════════════════════════════════════════

/**
 * Fetch a random FAQ question from the server and send it as a chat message.
 * The button is disabled with a spin animation while the request is in flight
 * to prevent double-clicks.
 */
randomQuestionBtn.addEventListener('click', function () {
    if (!isConnected || !currentUser) return;

    randomQuestionBtn.disabled = true;
    randomQuestionBtn.classList.add('spinning');

    fetch('/api/chatbot/random-question')
        .then(res => res.json())
        .then(data => {
            const question = data.question;
            if (question) {
                messageInput.value = question;
                messageForm.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
            }
        })
        .catch(err => console.error('Failed to fetch random question:', err))
        .finally(() => {
            randomQuestionBtn.disabled = false;
            randomQuestionBtn.classList.remove('spinning');
        });
});

// ═══════════════════════════════════════════════════════════════════
//  FAQ PANEL
// ═══════════════════════════════════════════════════════════════════

const CATEGORY_ICONS = {
    general: '💬', features: '✨', chatbot: '🤖',
    technology: '⚙️', architecture: '🏗️', usage: '📖'
};

/** Open the FAQ slide-in panel and load questions on first open. */
function openFaqPanel() {
    faqPanel.classList.remove('hidden');
    faqOverlay.classList.remove('hidden');
    requestAnimationFrame(() => faqPanel.classList.add('open'));
    if (faqList.querySelector('.faq-loading')) loadFaqQuestions();
}

/** Close the FAQ panel. */
function closeFaqPanel() {
    faqPanel.classList.remove('open');
    faqOverlay.classList.add('hidden');
    faqPanel.addEventListener('transitionend', () => faqPanel.classList.add('hidden'), { once: true });
}

/**
 * Fetch all FAQ questions from the server and render them
 * grouped by category inside the FAQ panel.
 */
function loadFaqQuestions() {
    fetch('/api/chatbot/questions')
        .then(res => res.json())
        .then(questions => {
            // Group by category
            const groups = {};
            questions.forEach(q => {
                const cat = q.category || 'general';
                if (!groups[cat]) groups[cat] = [];
                groups[cat].push(q.question);
            });

            faqList.innerHTML = '';
            Object.entries(groups).forEach(([cat, qs]) => {
                const group = document.createElement('div');
                group.className = 'faq-category-group';

                const label = document.createElement('div');
                label.className = 'faq-category-label';
                const icon = CATEGORY_ICONS[cat] || '📌';
                label.textContent = `${icon} ${cat.charAt(0).toUpperCase() + cat.slice(1)}`;
                group.appendChild(label);

                qs.forEach(question => {
                    const btn = document.createElement('button');
                    btn.className = 'faq-question';
                    btn.textContent = question;
                    btn.addEventListener('click', () => {
                        closeFaqPanel();
                        messageInput.value = question;
                        if (isConnected && currentUser) {
                            messageForm.dispatchEvent(
                                new Event('submit', { cancelable: true, bubbles: true })
                            );
                        }
                    });
                    group.appendChild(btn);
                });

                faqList.appendChild(group);
            });
        })
        .catch(err => {
            faqList.innerHTML = '<div class="faq-loading">Failed to load questions.</div>';
            console.error('Failed to load FAQ questions:', err);
        });
}

faqBtn.addEventListener('click', openFaqPanel);
faqCloseBtn.addEventListener('click', closeFaqPanel);
faqOverlay.addEventListener('click', closeFaqPanel);

// ═══════════════════════════════════════════════════════════════════
//  USER COUNT
// ═══════════════════════════════════════════════════════════════════

/**
 * Fetch total registered user count from the REST API
 * and update the badge in the header.
 */
function fetchUserCount() {
    fetch('/api/users/count')
        .then(res => res.json())
        .then(data => {
            totalUserCount.textContent = data.count;
        })
        .catch(err => console.error('Failed to fetch user count:', err));
}

// ═══════════════════════════════════════════════════════════════════
//  SWITCH USER MODAL
// ═══════════════════════════════════════════════════════════════════

/** Open the switch-user modal and load user list. */
function openSwitchUserModal() {
    switchUserOverlay.classList.remove('hidden');
    switchUserModal.classList.remove('hidden');
    loadSwitchUserList();
}

/** Close the switch-user modal. */
function closeSwitchUserModal() {
    switchUserOverlay.classList.add('hidden');
    switchUserModal.classList.add('hidden');
    switchNewUsername.value = '';
}

/**
 * Fetch all registered users and render them as clickable items
 * in the switch-user modal.
 */
function loadSwitchUserList() {
    fetch('/api/users')
        .then(res => res.json())
        .then(users => {
            switchUserList.innerHTML = '';
            if (users.length === 0) {
                switchUserList.innerHTML = '<div class="faq-loading">No registered users yet.</div>';
                return;
            }

            users.forEach(user => {
                const isCurrentUser = user.username === currentUser;
                const color = getAvatarColor(user.username);
                const initials = user.username.substring(0, 2).toUpperCase();
                const statusClass = user.status === 'ONLINE' ? 'online' : 'offline';

                const btn = document.createElement('button');
                btn.className = `switch-user-item${isCurrentUser ? ' active' : ''}`;
                btn.innerHTML = `
                    <div class="user-avatar" style="background: ${color}">${initials}</div>
                    <span>${escapeHtml(user.username)}</span>
                    ${isCurrentUser ? '<span class="you-tag">You</span>' : ''}
                    <span class="user-status-tag ${statusClass}">${user.status}</span>
                `;

                if (!isCurrentUser) {
                    btn.addEventListener('click', () => switchToUser(user.username));
                }

                switchUserList.appendChild(btn);
            });
        })
        .catch(err => {
            switchUserList.innerHTML = '<div class="faq-loading">Failed to load users.</div>';
            console.error('Failed to load users for switching:', err);
        });
}

/**
 * Switch to a different user.
 * Disconnects current WebSocket, clears UI, and reconnects as the new user.
 */
function switchToUser(newUsername) {
    if (!newUsername || newUsername.trim().length < 2) return;
    newUsername = newUsername.trim();
    if (newUsername === currentUser) return;

    closeSwitchUserModal();

    // Disconnect current user
    if (stompClient) {
        stompClient.disconnect();
    }
    fetch(`/api/users/disconnect?username=${encodeURIComponent(currentUser)}`, {
        method: 'POST'
    }).catch(() => { });

    // Reset state
    isConnected = false;
    stompClient = null;
    if (refreshInterval) { clearInterval(refreshInterval); refreshInterval = null; }

    // Clear messages
    messageArea.innerHTML = `
        <div class="welcome-message">
            <div class="welcome-icon">🔄</div>
            <h3>Switching to ${escapeHtml(newUsername)}...</h3>
            <p>Reconnecting...</p>
        </div>
    `;

    // Set new user and reconnect
    currentUser = newUsername;
    currentUserDisplay.textContent = currentUser;
    connectWebSocket();
}

// Wire up switch-user modal events
switchUserBtn.addEventListener('click', openSwitchUserModal);
switchModalClose.addEventListener('click', closeSwitchUserModal);
switchUserOverlay.addEventListener('click', closeSwitchUserModal);

switchNewUserBtn.addEventListener('click', function () {
    const newName = switchNewUsername.value.trim();
    if (newName.length >= 2) {
        switchToUser(newName);
    } else {
        shakeElement(switchNewUsername);
    }
});

switchNewUsername.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        switchNewUserBtn.click();
    }
});
