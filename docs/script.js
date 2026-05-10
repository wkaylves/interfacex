// 动态加载 CHANGELOG.md 并渲染到页面
document.addEventListener('DOMContentLoaded', function() {
    loadChangelog();
    setupSmoothScrolling();
});

// 加载更新日志
async function loadChangelog() {
    try {
        const response = await fetch('../CHANGELOG.md');
        if (response.ok) {
            const markdown = await response.text();
            const htmlContent = convertMarkdownToHtml(markdown);
            document.getElementById('changelog-container').innerHTML = htmlContent;
        } else {
            // 如果无法加载，显示提示信息
            document.getElementById('changelog-container').innerHTML = 
                '<p style="color: #7F8C8D; text-align: center;">更新日志加载中...</p>';
        }
    } catch (error) {
        console.error('Failed to load changelog:', error);
        document.getElementById('changelog-container').innerHTML = 
            '<p style="color: #e74c3c; text-align: center;">无法加载更新日志</p>';
    }
}

// 简单的 Markdown 转 HTML 转换器
function convertMarkdownToHtml(markdown) {
    let html = markdown;
    
    // 转换标题
    html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
    html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
    html = html.replace(/^#### (.+)$/gm, '<h4>$1</h4>');
    
    // 转换列表
    html = html.replace(/^\* (.+)$/gm, '<li>$1</li>');
    html = html.replace(/^- (.+)$/gm, '<li>$1</li>');
    
    // 将连续的 li 包裹在 ul 中
    html = html.replace(/((?:<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>');
    
    // 转换代码块
    html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
    
    // 转换行内代码
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    
    // 转换粗体
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    
    // 转换斜体
    html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
    
    // 转换链接
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');
    
    // 处理换行
    html = html.replace(/\n\n/g, '</p><p>');
    html = '<p>' + html + '</p>';
    
    // 清理空的 p 标签
    html = html.replace(/<p>\s*<\/p>/g, '');
    html = html.replace(/<p>\s*(<h[234]>)/g, '$1');
    html = html.replace(/(<\/h[234]>)\s*<\/p>/g, '$1');
    html = html.replace(/<p>\s*(<ul>)/g, '$1');
    html = html.replace(/(<\/ul>)\s*<\/p>/g, '$1');
    html = html.replace(/<p>\s*(<pre>)/g, '$1');
    html = html.replace(/(<\/pre>)\s*<\/p>/g, '$1');
    
    return html;
}

// 设置平滑滚动
function setupSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// 添加滚动时的导航栏效果
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    if (window.scrollY > 50) {
        navbar.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
    } else {
        navbar.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    }
});

// 添加特性卡片的动画效果
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// 观察所有特性卡片
document.addEventListener('DOMContentLoaded', function() {
    const cards = document.querySelectorAll('.feature-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = `opacity 0.6s ease-out ${index * 0.1}s, transform 0.6s ease-out ${index * 0.1}s`;
        observer.observe(card);
    });
});
