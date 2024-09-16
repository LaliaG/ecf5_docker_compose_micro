import React from 'react';
import Helmet from 'react-helmet';

const DocumentTitle = ({ title }) => {
    const defaultTitle = 'Ecommerce';
    return (
        <Helmet>
            <title>{title ? title : defaultTitle}</title>
        </Helmet>
    );
};

export { DocumentTitle };