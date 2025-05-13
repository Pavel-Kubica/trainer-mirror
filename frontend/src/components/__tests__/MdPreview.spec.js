import { mount } from '@vue/test-utils';
import { MdPreview } from 'md-editor-v3';

describe('MdPreview XSS Prevention', () => {
    it('should render safe HTML and prevent XSS attacks', () => {
        // Malicious content that attempts XSS
        const maliciousContent = '<img src=x onerror=alert("XSS") />';

        // Mount the MdPreview component with malicious content
        const wrapper = mount(MdPreview, {
            props: {
                language: 'en-US',
                previewTheme: 'github',
                theme: 'light',
                noIconfont: true,
                readOnly: true,
                toolbars: [],
                modelValue: maliciousContent
            }
        });

        // Check if the rendered HTML is safe
        const renderedHtml = wrapper.html();

        // Assert that the malicious script does not appear in the output
        expect(renderedHtml).not.toContain('<img src=x onerror=alert("XSS") />\'');
    });
    it('should render plain text content safely', () => {
        const plainTextContent = 'This is plain text content.';

        const wrapper = mount(MdPreview, {
            props: {
                language: 'en-US',
                previewTheme: 'github',
                theme: 'light',
                noIconfont: true,
                readOnly: true,
                toolbars: [],
                modelValue: plainTextContent
            }
        });

        const renderedHtml = wrapper.html();

        expect(renderedHtml).toContain(plainTextContent);
        expect(renderedHtml).not.toContain('<script>');
    });
    it('should render safe HTML content', () => {
        const safeHtmlContent = '<p>This is <strong>safe</strong> HTML content.</p>';

        const wrapper = mount(MdPreview, {
            props: {
                language: 'en-US',
                previewTheme: 'github',
                theme: 'light',
                noIconfont: true,
                readOnly: true,
                toolbars: [],
                modelValue: safeHtmlContent
            }
        });

        const renderedHtml = wrapper.html();

        expect(renderedHtml).toContain('<p>This is <strong>safe</strong> HTML content.</p>'); // Ensure safe HTML is rendered
        expect(renderedHtml).not.toContain('<script>'); // Ensure no script tags are present
    });

    it('should strip out unsafe HTML attributes', () => {
        const unsafeHtmlContent =
            '<p><a href="javascript:alert(\'XSS\')">Click me</a></p>' +
            '<script type="text/javascript">alert("hacked you");</script>' +
            '<img src="#" onerror=alert(1) />';

        const wrapper = mount(MdPreview, {
            props: {
                language: 'en-US',
                previewTheme: 'github',
                theme: 'light',
                noIconfont: true,
                readOnly: true,
                toolbars: [],
                modelValue: unsafeHtmlContent
            }
        });

        const renderedHtml = wrapper.html();

        expect(renderedHtml).toContain('Click me');
        expect(renderedHtml).not.toContain('javascript:alert');
        expect(renderedHtml).not.toContain('<script type="text/javascript">alert("hacked you");</script>');
        expect(renderedHtml).not.toContain('alert(1)');
    });
    it('should render Markdown content safely', () => {
        const markdownContent =
            '# Heading\n' +
            'This is **bold** and _italic_.\n' +
            'This is a [normal](https://pocasi.seznam.cz/praha) link.\n' +
            '[a](javascript:prompt(document.cookie))\n' +
            '[Basic](javascript:alert("Basic"))\n' +
            '[Local Storage](javascript:alert(JSON.stringify(localStorage)))\n' +
            '[CaseInsensitive](JaVaScRiPt:alert("CaseInsensitive"))\n' +
            '[URL](javascript://www.google.com%0Aalert("URL"))\n' +
            '![Uh oh...]("onerror="alert("XSS"))';

        const wrapper = mount(MdPreview, {
            props: {
                language: 'en-US',
                previewTheme: 'github',
                theme: 'light',
                noIconfont: true,
                readOnly: true,
                toolbars: [],
                modelValue: markdownContent
            }
        });

        const renderedHtml = wrapper.html();

        expect(renderedHtml).toContain('<strong>bold</strong>');
        expect(renderedHtml).toContain('<em>italic</em>');
        expect(renderedHtml).toContain('<a href="https://pocasi.seznam.cz/praha">normal</a>');
        expect(renderedHtml).not.toContain('<a href="javascript:prompt(document.cookie)">a</a>');
        expect(renderedHtml).not.toContain('<a href="javascript:alert("Basic")">Basic</a>');
        expect(renderedHtml).not.toContain('<a href="javascript:alert(JSON.stringify(localStorage))">Local Storage</a>');
        expect(renderedHtml).not.toContain('<a href="JaVaScRiPt:alert("CaseInsensitive")">CaseInsensitive</a>');
        expect(renderedHtml).not.toContain('<a href="javascript://www.google.com%0Aalert("URL")">URL</a>');
        expect(renderedHtml).not.toContain('<img src="onerror="alert("XSS")" alt="Uh oh...">');
        expect(renderedHtml).not.toContain('<script>');
    });

});
