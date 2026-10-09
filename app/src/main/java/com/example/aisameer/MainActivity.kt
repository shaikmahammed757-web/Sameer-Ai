<script>
    function startVoiceInput() {
        if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            const recognition = new SpeechRecognition();
            recognition.lang = 'te-IN'; // తెలుగు భాష (అవసరమైతే en-US కి మార్చుకోవచ్చు)
            recognition.interimResults = false;
            recognition.maxAlternatives = 1;

            recognition.onstart = function() {
                console.log("Listening...");
            };

            recognition.onresult = function(event) {
                const speechResult = event.results[0][0].transcript;
                document.getElementById('userInput').value = speechResult;
            };

            recognition.onerror = function(event) {
                alert("Mic Error: " + event.error);
            };

            recognition.start();
        } else {
            alert("Speech recognition not supported in this view.");
        }
    }

    function showResult(title, content, showUpload) {
        var box = document.getElementById('outputBox');
        box.style.display = 'block';
        var html = "<b>" + title + "</b><br><br>" + content;
        if(showUpload) {
            html += "<br><br><button onclick='uploadToYouTube()' style='background:#FF0000; color:white; border:none; padding:10px 16px; border-radius:8px; font-weight:bold; cursor:pointer;'>🚀 Upload to YouTube Studio</button>";
        }
        box.innerHTML = html;
    }

    function runAIAction(type) {
        var val = document.getElementById('userInput').value.trim();
        if(!val) val = "Riding & Technology Topic";

        if(type === 'youtube') {
            showResult("🎬 YouTube Script & Tags Result:", "1. Hook: Welcome back to SK MD Riding TV!\n2. Core Content: Explaining " + val + ".\n3. Outro: Subscribe for more tech videos!", true);
        } else if(type === 'image') {
            showResult("🖼️ AI Image Generation Result:", "Generated high-resolution graphic concept for: " + val);
        } else if(type === 'summary') {
            showResult("📄 Document Summary:", "Key takeaways and concise summary for: " + val + ".");
        } else if(type === 'ideas') {
            showResult("💡 Top Viral Ideas for " + val + ":\n1. Hidden Features\n2. Pro Level Guide\n3. Ultimate Review Setup", false);
        } else if(type === 'translate') {
            showResult("🔤 Translation Result:", "Translated \"" + val + "\" accurately into Telugu and English variants.", false);
        } else if(type === 'search') {
            AndroidApp.openWebSearch(val);
        }
    }

    function sendQuery() {
        var val = document.getElementById('userInput').value.trim();
        if(val) {
            runAIAction('youtube');
        } else {
            alert("Please enter a topic or question first!");
        }
    }

    function uploadToYouTube() {
        AndroidApp.openYouTubeUpload();
    }

    function switchNav(tab) {
        var box = document.getElementById('outputBox');
        if(tab === 'home') {
            box.style.display = 'none';
        } else {
            box.style.display = 'block';
            box.innerHTML = "<b>" + tab.toUpperCase() + " Section:</b><br><br>Feature active and ready.";
        }
    }
</script>
    
